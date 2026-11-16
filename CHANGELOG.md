# Changelog

All notable changes to Qatlas are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.1.2] - Unreleased

### Added
- **Export test cases of an execution** as **CSV** or **Excel (.xlsx)** from the
  "…" menu of the "Test Cases" table on the execution page.
  - "Export as CSV / Excel (all rows)" exports every test case of the execution.
  - "Export as CSV / Excel (filtered rows)" appears when a status filter or search
    is active and exports only the matching rows.
  - Shows an "Exporting…" indicator while the file is prepared, and a readable
    message if the export fails.
- **New REST endpoint** `GET /rs/test-execution/{id}/test-cases/export`
  (operationId `exportTestExecutionTestCases`):
  - `format` = `CSV` (default) or `XLSX`
  - optional `status` (repeatable) and `search` (test case / test suite name) filters
  - returns `404` if the execution does not exist
- Exported columns: Test Case ID, Test Suite, Test Case, Reference, Status,
  Start Time, End Time, Duration, Duration (ms), Total / Passed / Failed /
  Warning Steps, Comments.
- Excel files have a bold, frozen header row with filters and real date cells.
- Unit tests for the CSV and Excel writers.

### Notes
- Additive change only: no existing endpoint or api-client method was changed,
  so projects using `qatlas-client` keep working.
- The file is streamed, and the Excel writer needs no extra library, so the
  backend image does not grow.
- CSV cells starting with `=`, `+`, `-` or `@` are prefixed with `'` to prevent
  formula injection when the file is opened in a spreadsheet.

## [1.0.0] - 2026-09-04

### Added
- Initial public release of Qatlas, an open-source test reporting platform.
- **`qatlas-client`** — Java client library published to Maven Central
  (`org.qatlas:qatlas-client:1.0.0`), covering applications, environments,
  test executions, test suites, test cases, test steps, and attachments.
- **Backend** — Spring Boot REST API with MySQL persistence (Liquibase-managed
  schema), covering the full CRUD surface for the above entities.
- **`qatlas-ui`** — React-based web UI for browsing applications, environments,
  executions, and test results.
- Docker images for backend and UI, configurable via `docker-compose.yml` and
  a `.env` file (ports, DB credentials, published image version).

### Notes
- Requires a MySQL-compatible database (schema managed automatically via
  Liquibase on first startup).
- See `README.md` for local setup and `docker-compose.yml` for a
  container-based deployment.

[1.0.0]: https://github.com/qatlas-org/qatlas/releases/tag/v1.0.0
