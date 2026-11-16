package org.qatlas.backend.export;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * Writes rows of a table to an output stream, one row at a time, so large
 * exports never have to be built fully in memory.
 * <p>
 * Supported cell values: {@code null}, {@link String}, {@link Number},
 * {@link java.time.LocalDateTime}. Anything else is written with {@code toString()}.
 * <p>
 * {@link #close()} finishes the file but never closes the underlying stream.
 */
public interface TabularWriter extends Closeable {

    void writeRow(List<?> cells) throws IOException;

    @Override
    void close() throws IOException;
}
