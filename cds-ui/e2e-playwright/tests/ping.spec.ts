/*
 * ping.spec.ts
 *
 * Verifies that the CDS UI LoopBack back-end is reachable and that its
 * health-check endpoint (/ping) responds correctly.  The request is made
 * directly to the backend so this spec validates the server in isolation.
 */

import { test, expect } from '@playwright/test';

test.describe('Backend – /ping endpoint', () => {
  test('GET /ping returns a valid JSON greeting', async ({ request }) => {
    const response = await request.get('http://localhost:3000/ping');

    expect(response.status()).toBe(200);

    const body = await response.json();
    expect(body).toHaveProperty('greeting');
    expect(typeof body.greeting).toBe('string');
    expect(body.greeting.length).toBeGreaterThan(0);
  });

  test('GET /ping includes a date field', async ({ request }) => {
    const response = await request.get('http://localhost:3000/ping');
    const body = await response.json();

    expect(body).toHaveProperty('date');
    // The date should be parseable
    const d = new Date(body.date);
    expect(d.getTime()).not.toBeNaN();
  });

  test('GET /ping includes a url field matching the request path', async ({ request }) => {
    const response = await request.get('http://localhost:3000/ping');
    const body = await response.json();

    expect(body).toHaveProperty('url');
    expect(body.url).toBe('/ping');
  });
});
