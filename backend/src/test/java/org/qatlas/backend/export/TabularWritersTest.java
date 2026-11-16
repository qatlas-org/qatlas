package org.qatlas.backend.export;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TabularWritersTest {

    @Test
    void csvQuotesEscapesAndBlocksFormulas() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CsvWriter writer = new CsvWriter(out)) {
            writer.writeRow(List.of("Name", "Comment"));
            writer.writeRow(Arrays.asList("=HYPERLINK(\"x\")", null));
            writer.writeRow(List.of(42L, LocalDateTime.of(2026, 11, 16, 9, 30, 5)));
        }
        String csv = out.toString(StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("\uFEFF"), "UTF-8 BOM for Excel");
        assertEquals(
                "\uFEFF\"Name\",\"Comment\"\r\n"
                        + "\"'=HYPERLINK(\"\"x\"\")\",\"\"\r\n"
                        + "\"42\",\"2026-11-16 09:30:05\"\r\n",
                csv);
    }

    @Test
    void xlsxIsAValidZipWithEscapedContent() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XlsxWriter writer = new XlsxWriter(out, "Test Cases", new int[]{10, 20})) {
            writer.writeRow(List.of("ID", "Name"));
            writer.writeRow(List.of(1L, "a < b & \"c\"\u0001"));
        }

        Set<String> entries = new HashSet<>();
        String sheet = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                entries.add(entry.getName());
                if (entry.getName().equals("xl/worksheets/sheet1.xml")) {
                    sheet = new String(zip.readAllBytes(), StandardCharsets.UTF_8);
                }
            }
        }

        assertTrue(entries.containsAll(Set.of(
                "[Content_Types].xml", "_rels/.rels", "xl/workbook.xml",
                "xl/_rels/workbook.xml.rels", "xl/styles.xml", "xl/worksheets/sheet1.xml")));
        assertTrue(sheet.contains("<v>1</v>"));
        assertTrue(sheet.contains("a &lt; b &amp; &quot;c&quot;</t>"), "escaped, control char removed");
        assertTrue(sheet.contains("<autoFilter ref=\"A1:B2\"/>"));
    }

    @Test
    void columnNames() {
        assertEquals("A", XlsxWriter.columnName(0));
        assertEquals("Z", XlsxWriter.columnName(25));
        assertEquals("AA", XlsxWriter.columnName(26));
        assertEquals("AZ", XlsxWriter.columnName(51));
    }

    @Test
    void excelDateSerial() {
        assertEquals("45000.5", XlsxWriter.toExcelSerial(LocalDateTime.of(2023, 3, 15, 12, 0)));
    }

    @Test
    void sheetNameIsSanitized() {
        assertEquals("a_b_c", XlsxWriter.safeSheetName("a/b?c"));
        assertEquals(31, XlsxWriter.safeSheetName("x".repeat(40)).length());
    }
}
