package org.qatlas.backend.service.impl;

import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.enums.ExportFormat;
import org.qatlas.backend.export.CsvWriter;
import org.qatlas.backend.export.TabularWriter;
import org.qatlas.backend.export.XlsxWriter;
import org.qatlas.backend.service.TestCaseExportService;
import org.qatlas.backend.service.TestCaseService;
import org.qatlas.backend.service.TestExecutionService;
import org.qatlas.backend.vo.TestCaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class TestCaseExportServiceImpl implements TestCaseExportService {

    static final List<String> HEADERS = List.of(
            "Test Case ID",
            "Test Suite",
            "Test Case",
            "Reference",
            "Status",
            "Start Time",
            "End Time",
            "Duration",
            "Duration (ms)",
            "Total Steps",
            "Passed Steps",
            "Failed Steps",
            "Warning Steps",
            "Comments"
    );

    private static final int[] XLSX_COLUMN_WIDTHS = {12, 30, 40, 30, 12, 20, 20, 12, 14, 12, 13, 13, 14, 50};

    private final TestCaseService testCaseService;

    private final TestExecutionService testExecutionService;

    @Autowired
    public TestCaseExportServiceImpl(
            TestCaseService testCaseService,
            TestExecutionService testExecutionService) {
        this.testCaseService = testCaseService;
        this.testExecutionService = testExecutionService;
    }

    @Override
    public List<TestCaseVO> findTestCases(
            final Long testExecutionId,
            final ExecutionStatus[] executionStatuses,
            final String search) {
        // throws TestExecutionNotFoundException (-> 404) before any bytes are streamed
        testExecutionService.getById(testExecutionId);

        List<TestCaseVO> testCases = testCaseService.getAll(null, testExecutionId, executionStatuses);
        if (search == null || search.isBlank()) {
            return testCases;
        }
        String q = search.trim().toLowerCase(Locale.ROOT);
        return testCases.stream()
                .filter(tc -> contains(tc.getName(), q) || contains(tc.getTestSuiteName(), q))
                .toList();
    }

    @Override
    public void write(
            final List<TestCaseVO> testCases,
            final ExportFormat format,
            final OutputStream outputStream) throws IOException {
        try (TabularWriter writer = createWriter(format, outputStream)) {
            writer.writeRow(HEADERS);
            for (TestCaseVO testCase : testCases) {
                writer.writeRow(toRow(testCase));
            }
        }
    }

    private static TabularWriter createWriter(final ExportFormat format, final OutputStream outputStream)
            throws IOException {
        return switch (format) {
            case CSV -> new CsvWriter(outputStream);
            case XLSX -> new XlsxWriter(outputStream, "Test Cases", XLSX_COLUMN_WIDTHS);
        };
    }

    static List<Object> toRow(final TestCaseVO tc) {
        boolean finished = tc.getExecutionStartTime() != null && tc.getExecutionEndTime() != null;
        Long durationMs = finished ? tc.getExecutionTime() : null;
        return Arrays.asList(
                tc.getId(),
                tc.getTestSuiteName(),
                tc.getName(),
                tc.getReferenceId(),
                tc.getExecutionStatus() != null ? tc.getExecutionStatus().name() : null,
                tc.getExecutionStartTime(),
                tc.getExecutionEndTime(),
                durationMs != null ? formatDuration(durationMs) : null,
                durationMs,
                tc.getTotalTestStepCount(),
                tc.getPassedTestStepCount(),
                tc.getFailedTestStepCount(),
                tc.getTestStepCountWithWarnings(),
                tc.getComments()
        );
    }

    /** Same format as the UI's formatDuration(): "850ms", "42s", "3m 5s". */
    static String formatDuration(final long ms) {
        if (ms < 1000) {
            return ms + "ms";
        }
        long totalSeconds = Math.round(ms / 1000d);
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return m > 0 ? m + "m " + s + "s" : s + "s";
    }

    private static boolean contains(final String value, final String lowerCaseQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerCaseQuery);
    }
}
