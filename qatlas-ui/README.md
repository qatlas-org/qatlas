# QAtlas UI (React)

A new frontend for the QAtlas test-reporting backend, replacing the classic jQuery UI. Pure frontend — it does not replace or duplicate your existing Spring Boot backend, it just calls its `/rs/...` REST API.

## Stack & why

| Choice | Why |
|---|---|
| Vite + React + TypeScript | Fast dev server, small optimized prod builds, typed against the backend's actual DTOs |
| TanStack Query | Caches API responses so navigating execution → suite → case → steps doesn't re-fetch data you already loaded |
| TanStack Virtual | Virtualizes long lists (executions, test steps) so the DOM only ever renders ~15 visible rows, regardless of how many thousand rows exist in prod |
| Tailwind CSS | Utility CSS, no runtime CSS-in-JS cost |
| Recharts | Charts, bundled at build time |
| `@fontsource/inter` | Font bundled from npm (SIL Open Font License), **not** loaded from fonts.googleapis.com — no external request, no leaking user IPs to a third party, works behind restrictive office network/CSP |

**No runtime CDN dependencies at all** — everything (fonts, icons, charts, JS) is bundled at build time. This differs from the classic UI, which pulled Chart.js and a Font Awesome kit from external CDNs.

**Licensing**: every dependency is MIT / ISC / Apache-2.0 / SIL-OFL — all safe for internal/commercial office use, no copyleft obligations. Run `npm ls` / `npx license-checker` before adding new packages to keep it that way.

## Project structure

```
src/
  types/domain.ts        # TypeScript types mirroring the backend DTOs
  api/client.ts          # Typed Axios client for /rs/* endpoints
  hooks/useQueries.ts    # React Query hooks (caching layer)
  components/            # AppShell (nav), StatusBadge, shared primitives
  pages/                 # Dashboard, Executions (list+detail), Suite, TestCase, Applications, Environments
```

## Running locally

```bash
npm install
cp .env.example .env      # leave VITE_API_BASE_URL empty if using the dev proxy below
npm run dev
```

By default `vite.config.ts` proxies `/rs/*` to `http://localhost:8080` (override with `VITE_DEV_BACKEND_URL`) so the browser only ever talks to one origin in dev too — no CORS setup needed on the backend.

## Building & deploying

```bash
npm run build      # outputs to dist/
```

A `Dockerfile` + `nginx.conf` are included, following the same pattern as your existing `docker-compose.yml` `web` service: nginx serves the built static files and reverse-proxies `/rs/*` to the `backend` container, so the backend port never needs to be exposed to the browser directly.

To add it alongside your existing services, add something like:

```yaml
  web-react:
    build: ./qatlas-ui
    depends_on:
      - backend
    ports:
      - 9092:80
```

(Keep it side-by-side with the existing Angular `web` service on a different port until you're ready to cut over.)

## What's implemented

- Dashboard: recent-execution pass/fail chart, latest executions table
- Executions list: searchable, virtualized for large datasets
- Execution detail → its test suites
- Suite detail → its test cases
- Test case detail → its steps, with attachment file names
- Applications and Environments read-only lists

## What's not implemented yet (next steps)

- Create/edit/delete flows for Application, Environment, TestExecution (backend already supports these — just needs forms + mutations)
- Attachment image preview/download (currently just lists file names — backend's `FileRestController` `GET /rs/file` endpoint can serve the bytes)
- Archive / export / download-attachments actions from `TestExecutionRestController`
- Auth, if/when the backend adds it
