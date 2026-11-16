package org.qatlas.backend.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Minimal, dependency-free streaming writer for a single-sheet Excel (.xlsx) file.
 * <p>
 * Rows are written straight into the zipped sheet XML, so memory use stays flat
 * no matter how many rows are exported. The first row is treated as a header:
 * it is bold, frozen and gets an auto-filter.
 * <p>
 * Cell types: text (inline strings), numbers, and {@link LocalDateTime} (real
 * Excel dates shown as {@code yyyy-mm-dd hh:mm:ss}).
 */
public final class XlsxWriter implements TabularWriter {

    /** Excel's hard limit for text in one cell. */
    static final int MAX_CELL_TEXT_LENGTH = 32_767;

    private static final LocalDateTime EXCEL_EPOCH = LocalDateTime.of(1899, 12, 30, 0, 0);

    private static final int STYLE_DEFAULT = 0;
    private static final int STYLE_HEADER = 1;
    private static final int STYLE_DATE_TIME = 2;

    private final ZipOutputStream zip;
    private final Writer sheet;
    private int rowCount = 0;
    private int maxColumnCount = 0;

    /**
     * @param outputStream    target stream (not closed by this writer)
     * @param sheetName       name of the worksheet tab
     * @param columnWidths    optional column widths in characters (may be {@code null})
     */
    public XlsxWriter(final OutputStream outputStream, final String sheetName, final int[] columnWidths)
            throws IOException {
        this.zip = new ZipOutputStream(outputStream, StandardCharsets.UTF_8);
        writeEntry("[Content_Types].xml", CONTENT_TYPES);
        writeEntry("_rels/.rels", ROOT_RELS);
        writeEntry("xl/workbook.xml", workbookXml(sheetName));
        writeEntry("xl/_rels/workbook.xml.rels", WORKBOOK_RELS);
        writeEntry("xl/styles.xml", STYLES);

        zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
        this.sheet = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
        sheet.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sheet.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sheet.write("<sheetViews><sheetView workbookViewId=\"0\">"
                + "<pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/>"
                + "<selection pane=\"bottomLeft\" activeCell=\"A2\" sqref=\"A2\"/>"
                + "</sheetView></sheetViews>");
        if (columnWidths != null && columnWidths.length > 0) {
            sheet.write("<cols>");
            for (int i = 0; i < columnWidths.length; i++) {
                sheet.write("<col min=\"" + (i + 1) + "\" max=\"" + (i + 1)
                        + "\" width=\"" + columnWidths[i] + "\" customWidth=\"1\"/>");
            }
            sheet.write("</cols>");
        }
        sheet.write("<sheetData>");
    }

    @Override
    public void writeRow(final List<?> cells) throws IOException {
        rowCount++;
        maxColumnCount = Math.max(maxColumnCount, cells.size());
        boolean header = rowCount == 1;
        sheet.write("<row r=\"" + rowCount + "\">");
        for (int i = 0; i < cells.size(); i++) {
            writeCell(columnName(i) + rowCount, cells.get(i), header);
        }
        sheet.write("</row>");
    }

    private void writeCell(final String ref, final Object value, final boolean header) throws IOException {
        if (value == null) {
            return;
        }
        int style = header ? STYLE_HEADER : STYLE_DEFAULT;
        if (value instanceof LocalDateTime dateTime) {
            sheet.write("<c r=\"" + ref + "\" s=\"" + STYLE_DATE_TIME + "\"><v>"
                    + toExcelSerial(dateTime) + "</v></c>");
        } else if (value instanceof Number number && isFinite(number)) {
            sheet.write("<c r=\"" + ref + "\" s=\"" + style + "\"><v>" + number + "</v></c>");
        } else {
            String text = value.toString();
            if (text.length() > MAX_CELL_TEXT_LENGTH) {
                text = text.substring(0, MAX_CELL_TEXT_LENGTH);
            }
            sheet.write("<c r=\"" + ref + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">");
            sheet.write(escapeXml(text));
            sheet.write("</t></is></c>");
        }
    }

    @Override
    public void close() throws IOException {
        sheet.write("</sheetData>");
        if (rowCount > 0 && maxColumnCount > 0) {
            sheet.write("<autoFilter ref=\"A1:" + columnName(maxColumnCount - 1) + rowCount + "\"/>");
        }
        sheet.write("</worksheet>");
        sheet.flush();
        zip.closeEntry();
        // finish() completes the zip without closing the caller's stream
        zip.finish();
        zip.flush();
    }

    // ---------------------------------------------------------------- helpers

    private void writeEntry(final String name, final String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    /** 0 -> A, 25 -> Z, 26 -> AA ... */
    static String columnName(final int index) {
        StringBuilder sb = new StringBuilder();
        int n = index + 1;
        while (n > 0) {
            int rem = (n - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            n = (n - 1) / 26;
        }
        return sb.toString();
    }

    static String toExcelSerial(final LocalDateTime dateTime) {
        double days = Duration.between(EXCEL_EPOCH, dateTime).toMillis() / 86_400_000d;
        return Double.toString(days);
    }

    private static boolean isFinite(final Number number) {
        if (number instanceof Double d) {
            return Double.isFinite(d);
        }
        if (number instanceof Float f) {
            return Float.isFinite(f);
        }
        return true;
    }

    /** Escapes XML special chars and drops characters that are not allowed in XML 1.0. */
    static String escapeXml(final String text) {
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> {
                    if (c == '\t' || c == '\n' || c == '\r' || (c >= 0x20 && c <= 0xD7FF)
                            || (c >= 0xE000 && c <= 0xFFFD)) {
                        sb.append(c);
                    } else if (Character.isHighSurrogate(c) && i + 1 < text.length()
                            && Character.isLowSurrogate(text.charAt(i + 1))) {
                        sb.append(c).append(text.charAt(++i));
                    }
                    // else: invalid XML char (control char / lone surrogate) -> dropped
                }
            }
        }
        return sb.toString();
    }

    private static String workbookXml(final String sheetName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\""
                + " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"" + escapeXml(safeSheetName(sheetName)) + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    /** Excel sheet names: max 31 chars, none of {@code : \ / ? * [ ]}. */
    static String safeSheetName(final String name) {
        String cleaned = (name == null || name.isBlank() ? "Sheet1" : name).replaceAll("[:\\\\/?*\\[\\]]", "_");
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    private static final String CONTENT_TYPES =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                    + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                    + "</Types>";

    private static final String ROOT_RELS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                    + "</Relationships>";

    private static final String WORKBOOK_RELS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                    + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                    + "</Relationships>";

    /** cellXfs: 0 = default, 1 = bold header with grey fill, 2 = date-time. */
    private static final String STYLES =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                    + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"yyyy-mm-dd hh:mm:ss\"/></numFmts>"
                    + "<fonts count=\"2\">"
                    + "<font><sz val=\"11\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                    + "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/><family val=\"2\"/></font>"
                    + "</fonts>"
                    + "<fills count=\"3\">"
                    + "<fill><patternFill patternType=\"none\"/></fill>"
                    + "<fill><patternFill patternType=\"gray125\"/></fill>"
                    + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFE2E8F0\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                    + "</fills>"
                    + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                    + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                    + "<cellXfs count=\"3\">"
                    + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                    + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
                    + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                    + "</cellXfs>"
                    + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                    + "</styleSheet>";
}
