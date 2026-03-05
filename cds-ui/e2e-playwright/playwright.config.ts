/*
 * playwright.config.ts
 *
 * End-to-end test configuration for the CDS UI.
 *
 * Three web servers are started before tests run:
 *   0. Mock blueprints-processor – pure-Node stub on port 8080.
 *   1. Backend – LoopBack 4 server in HTTP mode on port 3000.
 *   2. Frontend – Angular dev-server on port 4200 (proxied to the backend).
 *
 * Run with:
 *   npm test               # headless Firefox
 *   npm run test:headed    # with browser visible
 *   npm run test:ui        # Playwright UI mode
 */

import { defineConfig, devices } from '@playwright/test';
import * as path from 'path';

const E2E_DIR = __dirname;
const SERVER_DIR = path.resolve(E2E_DIR, '../server');
const CLIENT_DIR = path.resolve(E2E_DIR, '../designer-client');
const PROXY_CONF = path.resolve(E2E_DIR, 'proxy.conf.test.json');
const BACKEND_SCRIPT = path.resolve(E2E_DIR, 'start-backend-http.js');
const MOCK_PROCESSOR_SCRIPT = path.resolve(E2E_DIR, 'mock-processor/server.js');

export default defineConfig({
  testDir: './tests',

  /* Never run specs in parallel – both the frontend and backend are shared */
  fullyParallel: false,
  workers: 1,

  /* Fail fast in CI, allow retries */
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,

  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
  ],

  use: {
    /* All tests navigate relative to the Angular dev-server */
    baseURL: 'http://localhost:4200',

    /* Record a full Playwright trace for every test so the complete session
     * (DOM snapshots, network traffic, console logs, action timeline) can be
     * inspected in the HTML report via "View trace". */
    trace: 'on',

    /* Record a video of every test run – embedded in the HTML report. */
    video: 'off',

    /* Always capture a screenshot at the end of each test. */
    screenshot: 'on',

    /* Allow self-signed certs in case HTTPS leaks through */
    ignoreHTTPSErrors: true,
  },

  /* --------------------------------------------------------------------- */
  /* Web servers                                                             */
  /* --------------------------------------------------------------------- */
  webServer: [
    /* ---- 0. Mock blueprints-processor ---------------------------------- */
    /*
     * Pure-Node stub that stands in for the real Spring-Boot service.
     * Listens on port 8080 – the default value of
     * API_BLUEPRINT_PROCESSOR_HTTP_BASE_URL in the LoopBack server config –
     * so no environment variable overrides are required.
     *
     * The mock is intentionally started first so it is reachable as soon as
     * the LoopBack BFF begins accepting requests.
     */
    {
      command: `node "${MOCK_PROCESSOR_SCRIPT}"`,
      cwd: E2E_DIR,
      /* Health-check: the blueprint list endpoint returns 200 + JSON array */
      url: 'http://localhost:8080/api/v1/blueprint-model/',
      reuseExistingServer: !process.env.CI,
      timeout: 15_000,
      stdout: 'pipe',
      stderr: 'pipe',
    },

    /* ---- 1. LoopBack back-end in HTTP mode ----------------------------- */
    {
      /* Build the TypeScript sources, then start the server without TLS.   */
      command: `npm run build && node "${BACKEND_SCRIPT}"`,
      cwd: SERVER_DIR,
      url: 'http://localhost:3000/ping',
      reuseExistingServer: !process.env.CI,
      timeout: 180_000, // 3 min – tsc compile can be slow
      stdout: 'pipe',
      stderr: 'pipe',
    },

    /* ---- 2. Angular dev-server ----------------------------------------- */
    /*
     * Serve the frontend with a test-specific proxy that routes API calls
     * to the HTTP backend (instead of the default HTTPS target).
     * NODE_OPTIONS is required because Angular 8 uses Webpack 4, which
     * relies on a legacy OpenSSL MD4 hash unavailable in Node ≥ 17.
     */
    {
      command: `NODE_OPTIONS=--openssl-legacy-provider npx ng serve --port 4200 --proxy-config "${PROXY_CONF}"`,
      cwd: CLIENT_DIR,
      url: 'http://localhost:4200',
      reuseExistingServer: !process.env.CI,
      timeout: 180_000, // 3 min – Angular initial compilation takes a while
      stdout: 'pipe',
      stderr: 'pipe',
    },
  ],

  /* --------------------------------------------------------------------- */
  /* Browser projects                                                        */
  /* --------------------------------------------------------------------- */
  projects: [
    {
      name: 'firefox',
      use: { ...devices['Desktop Firefox'] },
    },
  ],
});
