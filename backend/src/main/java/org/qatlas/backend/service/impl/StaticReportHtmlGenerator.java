package org.qatlas.backend.service.impl;

import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.vo.TestCaseVO;
import org.qatlas.backend.vo.TestExecutionVO;
import org.qatlas.backend.vo.TestStepAttachmentVO;
import org.qatlas.backend.vo.TestStepVO;
import org.qatlas.backend.vo.TestSuiteVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Builds a single, fully self-contained HTML report for one test execution:
 * inline CSS, inline vanilla JS (collapsible sections use native
 * &lt;details&gt;, screenshots use a small hand-rolled lightbox), and no
 * calls to any backend endpoint, CDN, or external resource. Meant to be
 * opened directly from disk (file://) with zero server required, so it
 * can be zipped alongside its attachment images and handed to a client
 * or dropped anywhere for offline viewing.
 */
@Component
public class StaticReportHtmlGenerator {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

    public String build(
            final TestExecutionVO execution,
            final List<TestSuiteVO> testSuites) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        html.append("<title>").append(escape(execution.getName())).append(" - QAtlas Report</title>\n");
        html.append("<style>").append(CSS).append("</style>\n");
        html.append("</head>\n<body>\n");

        appendHeader(html, execution);
        appendSummary(html, execution);

        html.append("<main class=\"suites\">\n");
        if (testSuites != null) {
            for (TestSuiteVO suite : testSuites) {
                appendSuite(html, suite);
            }
        }
        html.append("</main>\n");

        appendLightbox(html);
        html.append("<script>").append(JS).append("</script>\n");
        html.append("</body>\n</html>\n");
        return html.toString();
    }

    private void appendHeader(final StringBuilder html, final TestExecutionVO execution) {
        html.append("<header class=\"exec-header\">\n");
        html.append("<h1>").append(escape(execution.getName())).append("</h1>\n");
        html.append("<dl class=\"meta-grid\">\n");
        appendMeta(html, "Application", execution.getApplicationName());
        appendMeta(html, "Version", execution.getApplicationVersion());
        appendMeta(html, "Environment", execution.getEnvironmentName());
        appendMeta(html, "Browser", execution.getBrowser());
        appendMeta(html, "OS", execution.getOperatingSystem());
        appendMeta(html, "Executed By", execution.getExecutedBy());
        appendMeta(html, "System", execution.getSystemName());
        appendMeta(html, "Start Time", formatDate(execution.getStartTime()));
        appendMeta(html, "End Time", formatDate(execution.getEndTime()));
        html.append("</dl>\n</header>\n");
    }

    private void appendMeta(final StringBuilder html, final String label, final String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        html.append("<div class=\"meta-item\"><dt>")
            .append(escape(label))
            .append("</dt><dd>")
            .append(escape(value))
            .append("</dd></div>\n");
    }

    private void appendSummary(final StringBuilder html, final TestExecutionVO execution) {
        html.append("<section class=\"summary-bar\">\n");
        appendStat(html, "Targeted", execution.getTargetedTestCaseCount(), "targeted");
        appendStat(html, "Executed", execution.getExecutedTestCaseCount(), "executed");
        appendStat(html, "Passed", execution.getPassedTestCaseCount(), "passed");
        appendStat(html, "Failed", execution.getFailedTestCaseCount(), "failed");
        appendStat(html, "Skipped", execution.getSkippedTestCaseCount(), "skipped");
        appendStat(html, "In Progress", execution.getInProgressTestCaseCount(), "progress");
        html.append("</section>\n");
    }

    private void appendStat(final StringBuilder html, final String label, final int value, final String cssClass) {
        html.append("<div class=\"stat stat-").append(cssClass).append("\">")
            .append("<span class=\"stat-value\">").append(value).append("</span>")
            .append("<span class=\"stat-label\">").append(escape(label)).append("</span>")
            .append("</div>\n");
    }

    private void appendSuite(final StringBuilder html, final TestSuiteVO suite) {
        html.append("<details class=\"suite\" open>\n");
        html.append("<summary>Test Suite: ")
            .append(escape(suite.getTestSuiteName()))
            .append(" <span class=\"suite-counts\">(")
            .append(suite.getPassedTestCaseCount()).append(" passed, ")
            .append(suite.getFailedTestCaseCount()).append(" failed, ")
            .append(collectionSize(suite.getTestCases())).append(" total)</span>")
            .append("</summary>\n<div class=\"suite-body\">\n");

        if (suite.getTestCases() != null) {
            for (TestCaseVO testCase : suite.getTestCases()) {
                appendTestCase(html, testCase);
            }
        }
        html.append("</div>\n</details>\n");
    }

    private void appendTestCase(final StringBuilder html, final TestCaseVO testCase) {
        String statusClass = statusClass(testCase.getExecutionStatus());
        html.append("<details class=\"test-case\">\n");
        html.append("<summary><span class=\"badge badge-").append(statusClass).append("\">")
            .append(escape(statusLabel(testCase.getExecutionStatus())))
            .append("</span> ")
            .append(escape(testCase.getName()))
            .append("</summary>\n<div class=\"test-case-body\">\n");

        if (testCase.getComments() != null && !testCase.getComments().isBlank()) {
            html.append("<p class=\"comments\"><strong>Comments:</strong> ")
                .append(escape(testCase.getComments()))
                .append("</p>\n");
        }

        if (testCase.getTestSteps() != null && !testCase.getTestSteps().isEmpty()) {
            html.append("<table class=\"steps-table\">\n<thead><tr>")
                .append("<th>#</th><th>Description</th><th>Object</th><th>Operation</th>")
                .append("<th>Result</th><th>Status</th><th>Attachments</th>")
                .append("</tr></thead>\n<tbody>\n");
            int stepNumber = 1;
            for (TestStepVO step : testCase.getTestSteps()) {
                appendTestStep(html, step, stepNumber++);
            }
            html.append("</tbody>\n</table>\n");
        }
        html.append("</div>\n</details>\n");
    }

    private void appendTestStep(final StringBuilder html, final TestStepVO step, final int stepNumber) {
        String statusClass = statusClass(step.getExecutionStatus());
        html.append("<tr>")
            .append("<td>").append(stepNumber).append("</td>")
            .append("<td>").append(escape(step.getDescription())).append("</td>")
            .append("<td>").append(escape(step.getObjectName())).append("</td>")
            .append("<td>").append(escape(step.getOperation())).append("</td>")
            .append("<td>").append(escape(step.getResult())).append("</td>")
            .append("<td><span class=\"badge badge-").append(statusClass).append("\">")
            .append(escape(statusLabel(step.getExecutionStatus()))).append("</span></td>")
            .append("<td class=\"attachments-cell\">");

        if (step.getAttachments() != null) {
            for (TestStepAttachmentVO attachment : step.getAttachments()) {
                String relativePath = rawAttachmentPath(attachment);
                if (relativePath == null) {
                    continue;
                }
                String zipPath = "attachments/" + relativePath;
                html.append("<img class=\"thumb\" src=\"")
                    .append(escapeAttr(zipPath))
                    .append("\" alt=\"")
                    .append(escapeAttr(attachment.getFileName()))
                    .append("\" onclick=\"openLightbox(this.src)\">");
            }
        }
        html.append("</td></tr>\n");
    }

    private void appendLightbox(final StringBuilder html) {
        html.append("<div id=\"lightbox\" class=\"lightbox\" onclick=\"closeLightbox()\">")
            .append("<img id=\"lightbox-img\" src=\"\" alt=\"\">")
            .append("</div>\n");
    }

    /**
     * TestStepAttachmentMapper prefixes this with "../attachments/" for the
     * live app's relative-URL resolution. For the static export we only
     * need the raw path underneath the storage root, since the zip lays
     * attachments out fresh under its own "attachments/" folder next to
     * report.html.
     */
    private String rawAttachmentPath(final TestStepAttachmentVO attachment) {
        String path = attachment.getAttachmentRelativePath();
        if (path == null) {
            return null;
        }
        return path.replaceFirst("^\\.\\./attachments/", "");
    }

    private String statusClass(final ExecutionStatus status) {
        if (status == null) {
            return "planned";
        }
        return status.name().toLowerCase();
    }

    private String statusLabel(final ExecutionStatus status) {
        return status == null ? "Planned" : status.name();
    }

    private String formatDate(final LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATE_FORMAT);
    }

    private int collectionSize(final List<TestCaseVO> testCases) {
        return testCases == null ? 0 : testCases.size();
    }

    private String escape(final String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private String escapeAttr(final String value) {
        return escape(value);
    }

    private static final String CSS = """
        :root {
          --passed: #16a34a; --failed: #dc2626; --warning: #d97706;
          --progress: #2563eb; --planned: #6b7280;
          --bg: #f8fafc; --border: #e2e8f0; --text: #1e293b; --muted: #64748b;
        }
        * { box-sizing: border-box; }
        body {
          margin: 0; padding: 24px; background: var(--bg); color: var(--text);
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Arial, sans-serif;
          font-size: 14px; line-height: 1.5;
        }
        .exec-header { max-width: 1100px; margin: 0 auto 16px; }
        .exec-header h1 { font-size: 22px; margin: 0 0 12px; }
        .meta-grid { display: flex; flex-wrap: wrap; gap: 0; margin: 0; padding: 12px 16px;
          background: #fff; border: 1px solid var(--border); border-radius: 8px; }
        .meta-item { display: flex; flex-direction: column; margin: 4px 20px 4px 0; min-width: 100px; }
        .meta-item dt { font-size: 11px; text-transform: uppercase; letter-spacing: .03em; color: var(--muted); }
        .meta-item dd { margin: 0; font-weight: 600; }
        .summary-bar { max-width: 1100px; margin: 0 auto 20px; display: flex; gap: 10px; flex-wrap: wrap; }
        .stat { flex: 1 1 100px; background: #fff; border: 1px solid var(--border); border-radius: 8px;
          padding: 10px 14px; text-align: center; }
        .stat-value { display: block; font-size: 22px; font-weight: 700; }
        .stat-label { display: block; font-size: 11px; color: var(--muted); text-transform: uppercase; }
        .stat-passed .stat-value { color: var(--passed); }
        .stat-failed .stat-value { color: var(--failed); }
        .stat-progress .stat-value { color: var(--progress); }
        .suites { max-width: 1100px; margin: 0 auto; }
        details.suite { background: #fff; border: 1px solid var(--border); border-radius: 8px;
          margin-bottom: 12px; padding: 12px 16px; }
        details.suite > summary { font-size: 16px; font-weight: 700; cursor: pointer; }
        .suite-counts { font-weight: 400; color: var(--muted); font-size: 13px; }
        .suite-body { margin-top: 12px; }
        details.test-case { border: 1px solid var(--border); border-radius: 6px; margin-bottom: 8px; padding: 8px 12px; }
        details.test-case > summary { cursor: pointer; font-weight: 600; }
        .test-case-body { margin-top: 10px; }
        .comments { background: #f1f5f9; padding: 8px 10px; border-radius: 6px; }
        .badge { display: inline-block; padding: 2px 8px; border-radius: 999px; font-size: 11px;
          font-weight: 700; text-transform: uppercase; color: #fff; }
        .badge-passed { background: var(--passed); }
        .badge-failed { background: var(--failed); }
        .badge-warning { background: var(--warning); }
        .badge-progress { background: var(--progress); }
        .badge-planned { background: var(--planned); }
        .steps-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
        .steps-table th, .steps-table td { border: 1px solid var(--border); padding: 6px 8px;
          text-align: left; vertical-align: top; font-size: 13px; }
        .steps-table th { background: #f1f5f9; }
        .attachments-cell { white-space: nowrap; }
        .thumb { height: 40px; width: auto; border-radius: 4px; margin: 2px; cursor: pointer;
          border: 1px solid var(--border); }
        .lightbox { display: none; position: fixed; inset: 0; background: rgba(15,23,42,.85);
          align-items: center; justify-content: center; z-index: 1000; cursor: zoom-out; }
        .lightbox.open { display: flex; }
        .lightbox img { max-width: 90vw; max-height: 90vh; border-radius: 6px; }
        """;

    private static final String JS = """
        function openLightbox(src) {
          var lb = document.getElementById('lightbox');
          document.getElementById('lightbox-img').src = src;
          lb.classList.add('open');
        }
        function closeLightbox() {
          document.getElementById('lightbox').classList.remove('open');
        }
        """;

}
