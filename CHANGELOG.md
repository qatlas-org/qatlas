# Changelog

All notable changes to Qatlas are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

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
