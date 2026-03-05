/*
 * start-backend-http.js
 *
 * Starts the CDS UI LoopBack server in plain HTTP mode (no TLS).
 * Used exclusively by Playwright e2e tests so the frontend dev-server proxy
 * can reach the backend without certificate issues.
 *
 * Run from the cds-ui/server directory after `npm run build`:
 *   node ../e2e-playwright/start-backend-http.js
 */

'use strict';

const path = require('path');

// Resolve the compiled server entry-point relative to this script's location.
const serverDist = path.resolve(__dirname, '../server/dist/src');
const { main } = require(serverDist);

const config = {
  rest: {
    protocol: 'http',
    port: process.env.PORT ? +process.env.PORT : 3000,
    host: process.env.HOST || 'localhost',
    openApiSpec: {
      setServersFromRequest: true,
    },
  },
};

main(config).catch(err => {
  console.error('Cannot start the CDS UI server in HTTP mode:', err);
  process.exit(1);
});
