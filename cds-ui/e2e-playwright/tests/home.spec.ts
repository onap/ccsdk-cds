/*
 * home.spec.ts
 *
 * Validates that the Angular application loads correctly and that the browser
 * is redirected to the default route (/packages).  This spec covers the
 * frontend-to-backend integration: the page shell must be delivered by the
 * LoopBack static file server (or the Angular dev-server in test mode).
 */

import { test, expect } from '@playwright/test';

test.describe('Application – initial load', () => {
  test('root URL redirects to the packages dashboard', async ({ page }) => {
    await page.goto('/');

    // The app uses hash-based routing (#/packages)
    await page.waitForURL(url => url.hash.includes('packages'), { timeout: 15_000 });

    expect(page.url()).toContain('packages');
  });

  test('page title is set', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    // Angular sets the document title; it should be non-empty
    const title = await page.title();
    expect(title.trim().length).toBeGreaterThan(0);
  });

  test('<app-root> is rendered by Angular', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const appRoot = page.locator('app-root');
    await expect(appRoot).toBeAttached();
  });

  test('router-outlet is present inside <app-root>', async ({ page }) => {
    await page.goto('/');
    await page.waitForLoadState('networkidle');

    const outlet = page.locator('app-root router-outlet');
    await expect(outlet).toBeAttached();
  });
});
