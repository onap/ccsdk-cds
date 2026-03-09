/*
 * mock-processor/server.js
 *
 * Lightweight stub for the CDS blueprints-processor REST API (api/v1).
 * Used exclusively by Playwright e2e tests so the full Angular → LoopBack →
 * upstream path is exercised without needing a live Spring-Boot service.
 *
 * Listens on the same host/port the LoopBack BFF expects by default:
 *   http://localhost:8080/api/v1
 *
 * Override the port with the MOCK_PROCESSOR_PORT environment variable.
 *
 * All responses are derived from the JSON fixture files in ./fixtures/.
 * POST/PUT/DELETE operations that mutate data operate on an in-memory copy of
 * the fixtures so tests remain independent across runs.
 *
 * Usage (from the e2e-playwright directory):
 *   node mock-processor/server.js
 */

'use strict';

const http = require('http');
const path = require('path');
const fs   = require('fs');
const url  = require('url');

// ── configuration ─────────────────────────────────────────────────────────────
const PORT     = process.env.MOCK_PROCESSOR_PORT ? +process.env.MOCK_PROCESSOR_PORT : 8080;
const FIXTURES = path.join(__dirname, 'fixtures');
const BASE     = '/api/v1';

// ── in-memory fixture data (deep-cloned so mutations stay in-process) ─────────
const blueprints = JSON.parse(
  fs.readFileSync(path.join(FIXTURES, 'blueprints.json'), 'utf8'));
const resourceDictionaries = JSON.parse(
  fs.readFileSync(path.join(FIXTURES, 'resource-dictionaries.json'), 'utf8'));
const modelTypes = JSON.parse(
  fs.readFileSync(path.join(FIXTURES, 'model-types.json'), 'utf8'));

// ── helpers ───────────────────────────────────────────────────────────────────

/** Send a JSON response. */
function json(res, data, status = 200) {
  const body = JSON.stringify(data);
  res.writeHead(status, {
    'Content-Type': 'application/json',
    'Content-Length': Buffer.byteLength(body),
  });
  res.end(body);
}

/** Drain the request body and resolve with the raw string. */
function readBody(req) {
  return new Promise((resolve) => {
    let raw = '';
    req.on('data', chunk => { raw += chunk; });
    req.on('end', () => resolve(raw));
  });
}

/**
 * Build a Spring-style Page response that mirrors what blueprints-processor
 * returns for the paged blueprint-model endpoints.
 */
function pagedResponse(items, query) {
  const limit  = Math.max(1, parseInt(query.limit,  10) || 20);
  const offset = Math.max(0, parseInt(query.offset, 10) || 0);
  const page   = Math.floor(offset / limit);
  const sliced = items.slice(offset, offset + limit);
  return {
    content:          sliced,
    pageable: {
      pageNumber:     page,
      pageSize:       limit,
      sort:           { sorted: true, unsorted: false, empty: false },
      offset:         offset,
      unpaged:        false,
      paged:          true,
    },
    totalElements:    items.length,
    totalPages:       Math.ceil(items.length / limit),
    size:             limit,
    number:           page,
    sort:             { sorted: true, unsorted: false, empty: false },
    first:            page === 0,
    last:             offset + sliced.length >= items.length,
    numberOfElements: sliced.length,
    empty:            sliced.length === 0,
  };
}

const CBA_ZIPS = path.join(FIXTURES, 'cba-zips');

/**
 * Load a real CBA ZIP from fixtures/cba-zips/ for the given name+version.
 * Falls back to a minimal 4-byte PK header if the file does not exist.
 */
function loadCbaZip(name, version) {
  const zipPath = path.join(CBA_ZIPS, `${name}-${version}.zip`);
  if (fs.existsSync(zipPath)) {
    return fs.readFileSync(zipPath);
  }
  // Fallback: smallest valid ZIP binary (4 bytes of PK magic).
  return Buffer.from('504b0304', 'hex');
}

// ── request handler ───────────────────────────────────────────────────────────

const server = http.createServer(async (req, res) => {
  const parsed   = url.parse(req.url, true);
  const pathname = parsed.pathname;
  const query    = parsed.query;
  const method   = req.method;

  process.stderr.write(`[mock-processor] ${method} ${pathname}\n`);

  let m; // reused for regex captures throughout

  // ── blueprint-model ──────────────────────────────────────────────────────────

  // GET /api/v1/blueprint-model/paged/meta-data/:keyword  (must precede generic paged)
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/blueprint-model/paged/meta-data/(.+)$`))) {
    const keyword  = decodeURIComponent(m[1]);
    const filtered = blueprints.filter(b =>
      b.artifactName.includes(keyword) ||
      (b.artifactDescription || '').includes(keyword) ||
      (b.tags || '').includes(keyword));
    return json(res, pagedResponse(filtered, query));
  }

  // GET /api/v1/blueprint-model/paged
  if (method === 'GET' && pathname === `${BASE}/blueprint-model/paged`) {
    let items = blueprints;
    if (query.published) {
      const pub = query.published === 'true' ? 'Y' : 'N';
      items = items.filter(b => b.published === pub);
    }
    return json(res, pagedResponse(items, query));
  }

  // GET /api/v1/blueprint-model/search/:tags
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/blueprint-model/search/(.+)$`))) {
    const tags   = decodeURIComponent(m[1]);
    const result = blueprints.filter(b => (b.tags || '').includes(tags));
    return json(res, result);
  }

  // GET /api/v1/blueprint-model/meta-data/:keyword
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/blueprint-model/meta-data/(.+)$`))) {
    const keyword = decodeURIComponent(m[1]);
    const result  = blueprints.filter(b =>
      b.artifactName.includes(keyword) ||
      (b.artifactDescription || '').includes(keyword) ||
      (b.tags || '').includes(keyword));
    return json(res, result);
  }

  // GET /api/v1/blueprint-model/by-name/:name/version/:version
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/blueprint-model/by-name/([^/]+)/version/([^/]+)$`))) {
    const [, name, version] = m;
    const bp = blueprints.find(
      b => b.artifactName === name && b.artifactVersion === version);
    return bp ? json(res, bp) : json(res, { error: 'not found' }, 404);
  }

  // GET /api/v1/blueprint-model/download/by-name/:name/version/:version
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/blueprint-model/download/by-name/([^/]+)/version/([^/]+)$`))) {
    const [, name, version] = m;
    const buf = loadCbaZip(decodeURIComponent(name), decodeURIComponent(version));
    res.writeHead(200, {
      'Content-Type':        'application/zip',
      'Content-Disposition': `attachment; filename="${name}-${version}.zip"`,
      'Content-Length':      buf.length,
    });
    return res.end(buf);
  }

  // GET /api/v1/blueprint-model   (list all)
  if (method === 'GET' && (pathname === `${BASE}/blueprint-model/` || pathname === `${BASE}/blueprint-model`)) {
    return json(res, blueprints);
  }

  // GET /api/v1/blueprint-model/:id
  // Returns the blueprint wrapped in an array so the LoopBack BFF's
  // responsePath "$.*" extracts it as [{ … }] rather than an array of
  // bare property values.
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/blueprint-model/([^/]+)$`))) {
    const bp = blueprints.find(b => b.id === m[1]);
    return bp ? json(res, [bp]) : json(res, { error: 'not found' }, 404);
  }

  // DELETE /api/v1/blueprint-model/:id
  if (method === 'DELETE' &&
      (m = pathname.match(`^${BASE}/blueprint-model/([^/]+)$`))) {
    return json(res, { message: 'deleted', id: m[1] });
  }

  // POST /api/v1/blueprint-model/enrich   – returns enriched CBA zip
  if (method === 'POST' && (pathname === `${BASE}/blueprint-model/enrich` || pathname === `${BASE}/blueprint-model/enrich/`)) {
    await readBody(req); // drain multipart body
    // Return the first available CBA zip as the 'enriched' result
    const firstBp = blueprints[0];
    const buf = firstBp
      ? loadCbaZip(firstBp.artifactName, firstBp.artifactVersion)
      : Buffer.from('504b0304', 'hex');
    res.writeHead(200, {
      'Content-Type':        'application/zip',
      'Content-Disposition': 'attachment; filename="enriched.zip"',
      'Content-Length':      buf.length,
    });
    return res.end(buf);
  }

  // POST upload stubs – drain multipart body, echo back first fixture blueprint
  if (method === 'POST' && (
    pathname === `${BASE}/blueprint-model` ||
    pathname === `${BASE}/blueprint-model/` ||
    pathname === `${BASE}/blueprint-model/publish` ||
    pathname === `${BASE}/blueprint-model/publish/` ||
    pathname === `${BASE}/blueprint-model/enrichandpublish` ||
    pathname === `${BASE}/blueprint-model/enrichandpublish/`
  )) {
    await readBody(req);
    return json(res, blueprints[0]);
  }

  // ── dictionary ───────────────────────────────────────────────────────────────

  // GET /api/v1/dictionary/paged   (must precede /:name)
  if (method === 'GET' && pathname === `${BASE}/dictionary/paged`) {
    return json(res, pagedResponse(resourceDictionaries, query));
  }

  // GET /api/v1/dictionary/source-mapping   (must precede /:name)
  if (method === 'GET' && pathname === `${BASE}/dictionary/source-mapping`) {
    return json(res, { INPUT: 'input', DEFAULT: 'default', DB: 'db', REST: 'rest' });
  }

  // GET /api/v1/dictionary/search/:tags
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/dictionary/search/(.+)$`))) {
    const tags   = decodeURIComponent(m[1]);
    const result = resourceDictionaries.filter(d => (d.tags || '').includes(tags));
    return json(res, result);
  }

  // GET /api/v1/dictionary/:name
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/dictionary/([^/]+)$`))) {
    const d = resourceDictionaries.find(r => r.name === m[1]);
    return d ? json(res, d) : json(res, { error: 'not found' }, 404);
  }

  // POST /api/v1/dictionary/by-names
  if (method === 'POST' && pathname === `${BASE}/dictionary/by-names`) {
    const raw = await readBody(req);
    try {
      const names  = JSON.parse(raw);
      const result = resourceDictionaries.filter(d =>
        Array.isArray(names) && names.includes(d.name));
      return json(res, result);
    } catch (_) {
      return json(res, []);
    }
  }

  // POST /api/v1/dictionary/definition   (must precede bare /dictionary)
  if (method === 'POST' && pathname === `${BASE}/dictionary/definition`) {
    const raw = await readBody(req);
    try { return json(res, JSON.parse(raw)); } catch (_) { return json(res, {}); }
  }

  // POST /api/v1/dictionary
  if (method === 'POST' && pathname === `${BASE}/dictionary`) {
    const raw = await readBody(req);
    try { return json(res, JSON.parse(raw)); } catch (_) { return json(res, {}); }
  }

  // ── model-type ───────────────────────────────────────────────────────────────

  // GET /api/v1/model-type/search/:tags   (must precede by-definition and /:name)
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/model-type/search/(.+)$`))) {
    const tags   = decodeURIComponent(m[1]);
    const result = modelTypes.filter(t => (t.tags || '').includes(tags));
    return json(res, result);
  }

  // GET /api/v1/model-type/by-definition/:type
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/model-type/by-definition/([^/]+)$`))) {
    const defType = decodeURIComponent(m[1]);
    const result  = modelTypes.filter(t => t.definitionType === defType);
    return json(res, result);
  }

  // GET /api/v1/model-type/:name
  if (method === 'GET' &&
      (m = pathname.match(`^${BASE}/model-type/([^/]+)$`))) {
    const t = modelTypes.find(mt => mt.modelName === m[1]);
    return t ? json(res, t) : json(res, { error: 'not found' }, 404);
  }

  // POST /api/v1/model-type
  if (method === 'POST' && pathname === `${BASE}/model-type`) {
    const raw = await readBody(req);
    try { return json(res, JSON.parse(raw)); } catch (_) { return json(res, {}); }
  }

  // DELETE /api/v1/model-type/:name
  if (method === 'DELETE' &&
      (m = pathname.match(`^${BASE}/model-type/([^/]+)$`))) {
    return json(res, { message: 'deleted', name: m[1] });
  }

  // ── fallthrough ───────────────────────────────────────────────────────────────
  process.stderr.write(`[mock-processor] 404 – no stub for ${method} ${pathname}\n`);
  json(res, { error: `no stub for ${method} ${pathname}` }, 404);
});

server.listen(PORT, 'localhost', () => {
  process.stderr.write(
    `[mock-processor] blueprints-processor stub listening on http://localhost:${PORT}\n`);
});
