package org.qatlas.backend.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RFC 4180 CSV writer.
 * <ul>
 *     <li>Starts with a UTF-8 BOM so Excel opens non-ASCII text correctly.</li>
 *     <li>Every cell is quoted; quotes are doubled; rows end with CRLF.</li>
 *     <li>Text starting with {@code = + - @} (or tab / CR) is prefixed with {@code '}
 *     to stop spreadsheet formula injection (OWASP "CSV injection").</li>
 * </ul>
 */
public final class CsvWriter implements TabularWriter {

    static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Writer writer;

    public CsvWriter(final OutputStream outputStream) throws IOException {
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        this.writer.write('\uFEFF');
    }

    @Override
    public void writeRow(final List<?> cells) throws IOException {
        for (int i = 0; i < cells.size(); i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write('"');
            writer.write(format(cells.get(i)).replace("\"", "\"\""));
            writer.write('"');
        }
        writer.write("\r\n");
    }

    private static String format(final Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime dateTime) {
            return DATE_TIME_FORMAT.format(dateTime);
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return neutralizeFormula(value.toString());
    }

    static String neutralizeFormula(final String text) {
        if (text.isEmpty()) {
            return text;
        }
        char first = text.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + text;
        }
        return text;
    }

    @Override
    public void close() throws IOException {
        // flush only: the caller owns the underlying stream
        writer.flush();
    }
}
