# CDS UI – Playwright End-to-End Tests

This directory contains Playwright e2e tests that exercise the full
Angular → LoopBack BFF → mock-processor stack without requiring a live
ONAP blueprints-processor instance.

## Architecture

```
Browser (Playwright / Angular dev-server :4200)
  │
  │  /controllerblueprint/*
  │  /resourcedictionary/*
  ▼
LoopBack 4 BFF (:3000)          ← TypeScript backend (cds-ui/server)
  │
  │  http://localhost:8080/api/v1/…
  ▼
mock-processor (:8080)          ← pure-Node HTTP stub (mock-processor/server.js)
```

All three services are started automatically by Playwright before any test
runs (and stopped afterwards). The mock-processor stands in for the real
Spring-Boot blueprints-processor, so the tests are fully self-contained and
require no external ONAP infrastructure.

## Directory layout

```
e2e-playwright/
├── package.json                 – npm scripts (dev, test, …)
├── tsconfig.json                – TypeScript config for test files
├── playwright.config.ts         – Playwright configuration (webServer, projects, …)
├── proxy.conf.test.json         – Angular dev-server proxy (HTTP target, port 3000)
├── start-backend-http.js        – Starts the LoopBack BFF in plain HTTP mode
├── start-dev.sh                 – Starts all three services without running tests
├── mock-processor/
│   ├── server.js                – Pure-Node HTTP stub for blueprints-processor API
│   └── fixtures/
│       ├── blueprints.json      – 4 sample blueprint-model records
│       ├── resource-dictionaries.json – 3 sample resource dictionary records
│       └── model-types.json     – 4 sample model-type / source-type records
└── tests/
    ├── ping.spec.ts             – LoopBack BFF health-check (/ping)
    ├── home.spec.ts             – Angular app bootstrap and routing
    ├── packages.spec.ts         – Packages Dashboard UI + API integration
    └── resource-dictionary.spec.ts – Resource Dictionary UI + API integration
```

## Prerequisites

| Tool    | Version |
| ------- | ------- |
| Node.js | ≥ 18    |
| npm     | ≥ 9     |

All npm dependencies live inside this directory and do **not** affect the
`server` or `designer-client` projects.

## One-time setup

```bash
cd cds-ui/e2e-playwright
npm install
npm run install:browsers   # downloads Chromium / Firefox binaries
```

## Running the tests

### Headless Firefox (CI default)

```bash
npm test
```

Playwright automatically starts all three services in order before tests run:

| #   | Service            | Port | Health-check URL               |
| --- | ------------------ | ---- | ------------------------------ |
| 0   | mock-processor     | 8080 | `GET /api/v1/blueprint-model/` |
| 1   | LoopBack BFF       | 3000 | `GET /ping`                    |
| 2   | Angular dev-server | 4200 | `GET /`                        |

All processes are stopped when the test run ends.

> **Note** – The first run can take up to 3 minutes because the LoopBack
> TypeScript sources must be compiled and Angular's initial Webpack build is
> slow. Subsequent runs are faster because `reuseExistingServer: true` (see
> below) lets Playwright skip startup if the ports are already occupied.

### Headed (see the browser)

```bash
npm run test:headed
```

### Playwright UI mode (interactive, great for authoring new tests)

```bash
npm run test:ui
```

### View the HTML report after a run

```bash
npm run report
```

## Starting services without running tests

Use this when you want to explore the UI in a browser, debug a specific page,
or run a single test in isolation without the full startup overhead on every
invocation.

```bash
cd cds-ui/e2e-playwright
npm run dev          # or: ./start-dev.sh
```

The script starts all three services in sequence, waits for each health-check
URL to return 200, then prints a ready message and blocks until you press
**Ctrl-C**:

```
[start-dev] mock-processor is ready.
[start-dev] LoopBack BFF is ready.
[start-dev] Angular dev-server is ready.

[start-dev] All services are running:
[start-dev]   mock-processor   → http://localhost:8080/api/v1/blueprint-model/
[start-dev]   LoopBack BFF     → http://localhost:3000/ping
[start-dev]   Angular UI       → http://localhost:4200

[start-dev] Press Ctrl-C to stop all services.
```

With the services running you can:

- Open **http://localhost:4200** in any browser.
- Run a specific test file without the startup delay:
  ```bash
  # in a second terminal
  cd cds-ui/e2e-playwright
  npx playwright test tests/packages.spec.ts
  ```
- Hit BFF endpoints directly, e.g.:
  ```bash
  curl http://localhost:3000/controllerblueprint/paged?limit=5&offset=0&sort=NAME&sortType=ASC
  curl http://localhost:8080/api/v1/blueprint-model/
  ```

Ctrl-C terminates all three processes cleanly via the EXIT trap in
`start-dev.sh`.

## How it works

### mock-processor (`mock-processor/server.js`)

A pure-Node `http.createServer` stub that listens on port 8080 and implements
every endpoint the LoopBack datasource templates call on the real
blueprints-processor REST API (`/api/v1/…`):

| Prefix                     | Endpoints stubbed                                                                                                                   |
| -------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| `/api/v1/blueprint-model/` | list all, paged, paged+keyword, search by tags, by-id, by-name+version, upload (create/publish/enrich/deploy), download ZIP, delete |
| `/api/v1/dictionary/`      | by-name, search by tags, source-mapping, save, save-definition, by-names                                                            |
| `/api/v1/model-type/`      | by-name, search by tags, by-definition type, save, delete                                                                           |

Responses are served from the JSON fixture files in `mock-processor/fixtures/`.
POST/DELETE operations that would mutate data operate on an in-memory copy of
the fixtures so the fixture files themselves are never changed.

Unknown routes return `404` with a JSON body so that test failures caused by a
missing stub are immediately identifiable in the logs.

The port can be overridden with the `MOCK_PROCESSOR_PORT` environment variable,
though no other config changes are needed when using the default (8080).

### LoopBack BFF (`start-backend-http.js`)

Imports the compiled LoopBack application from `../server/dist/src` and starts
it with `protocol: 'http'`. This bypasses the P12 keystore requirement that the
production entry-point (`index.js`) enforces.

The `webServer` entry in `playwright.config.ts` runs `npm run build` inside
`../server` before starting the process so the `dist/` output is always
up to date.

The BFF connects to the mock-processor via the same `API_BLUEPRINT_PROCESSOR_HTTP_BASE_URL`
environment variable it uses in production. The default value
(`http://localhost:8080/api/v1`) matches the mock, so no override is needed.

### Angular dev-server

Started with `proxy.conf.test.json` instead of the default `proxy.conf.json`.
The only difference is that the proxy target uses `http://` rather than
`https://`, matching the test-mode LoopBack server.

`NODE_OPTIONS=--openssl-legacy-provider` is required because Angular 8 uses
Webpack 4, which relies on a legacy OpenSSL MD4 hash that was removed in
Node.js ≥ 17.

### Reuse existing servers

When the `CI` environment variable is not set, `reuseExistingServer: true` in
`playwright.config.ts` means Playwright skips starting a service if its port is
already occupied. This is what makes `npm run dev` + a separate `npx playwright
test` invocation work efficiently: the servers started by `start-dev.sh` are
reused automatically.

## Test files

| File                          | What it tests                                                                                                                                                                 |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ping.spec.ts`                | LoopBack `/ping` endpoint shape and fields                                                                                                                                    |
| `home.spec.ts`                | Angular app bootstrap, redirect to `/#/packages`, `<app-root>` presence                                                                                                       |
| `packages.spec.ts`            | Dashboard structure, tabs, fixture data rendered in cards, search, sort, proxy wiring                                                                                         |
| `resource-dictionary.spec.ts` | Dashboard structure, tabs, search/filter sub-components, API proxy |

## Fixture data

The four blueprint records used throughout the tests:

| artifactName | artifactVersion | published | tags                |
| ------------ | --------------- | --------- | ------------------- |
| vFW-CDS      | 1.0.0           | Y         | vFW, firewall, demo |
| vDNS-CDS     | 2.0.0           | Y         | vDNS, dns, demo     |
| vLB-CDS      | 1.1.0           | N         | vLB, loadbalancer   |
| vPE-CDS      | 3.0.0           | Y         | vPE, archived, edge |
