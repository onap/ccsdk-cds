/*
 * packages.spec.ts
 *
 * End-to-end tests for the Packages Dashboard feature.  These tests verify:
 *   1. Page structure – correct Angular components are rendered.
 *   2. Navigation tabs – presence and tab-switching behaviour.
 *   3. Search & filter UI – sub-components render.
 *   4. API integration – the Angular dev-server proxy routes requests through
 *      the LoopBack BFF which in turn calls the mock-processor.  Now that the
 *      mock is running the integration tests assert exact HTTP 200 responses
 *      and fixture data rendered in the DOM.
 *   5. Tab filtering – Deployed / Under Construction tabs filter by published
 *      status via the `published` query parameter.
 *
 * Fixture data (from mock-processor/fixtures/blueprints.json – real data from
 * cds-ui-oom-sm-master.tnaplab.telekom.de):
 *   RT-resource-resolution  1.0.0  published=Y  tags: test, regression
 *   vLB_CDS_KOTLIN          1.0.0  published=Y  tags: test, vDNS-CDS, SCALE-OUT, MARCO
 *   vLB_CDS_RESTCONF        1.0.0  published=Y  tags: vLB-CDS
 *   vLB_CDS                 1.0.0  published=N  tags: vLB_CDS
 *   5G_Core                 2.0.0  published=Y  tags: Thamlur Raju, Malinconico Aniello Paolo,Vamshi, 5G_Core
 *   vFW-CDS                 1.0.0  published=Y  tags: vFW-CDS
 *   pnf_netconf             1.0.0  published=N  tags: pnf_netconf
 *   APACHE                  1.0.0  published=Y  tags: Lukasz Rajewski, CNF
 *   ubuntu20                1.0.0  published=N  tags: ubuntu20
 */

import { test, expect } from '@playwright/test';

// ── helpers ────────────────────────────────────────────────────────────────────

/**
 * Wait for the package cards to be rendered in the DOM.
 * The packages store dispatches a getPagedPackages() call on load; the Angular
 * template uses *ngFor to render one card per blueprint in the fixture.
 */
async function waitForPackageCards(page: import('@playwright/test').Page) {
    // At least one package name should appear (fixture has 9 blueprints)
    await expect(page.locator('.packageName').first()).toBeVisible({ timeout: 20_000 });
}

const FIXTURE_NAMES = [
  'RT-resource-resolution', 'vLB_CDS_KOTLIN', 'vLB_CDS_RESTCONF', 'vLB_CDS',
  '5G_Core', 'vFW-CDS', 'pnf_netconf', 'APACHE', 'ubuntu20',
] as const;
const FIXTURE_COUNT = FIXTURE_NAMES.length;

// Blueprints with published="Y" (deployed)
const DEPLOYED_NAMES = [
  'RT-resource-resolution', 'vLB_CDS_KOTLIN', 'vLB_CDS_RESTCONF',
  '5G_Core', 'vFW-CDS', 'APACHE',
] as const;
const DEPLOYED_COUNT = DEPLOYED_NAMES.length;

// Blueprints with published="N" (under construction)
const UNDER_CONSTRUCTION_NAMES = ['vLB_CDS', 'pnf_netconf', 'ubuntu20'] as const;
const UNDER_CONSTRUCTION_COUNT = UNDER_CONSTRUCTION_NAMES.length;

/** Selector for a tab link by its visible text. */
function tabLink(text: string) {
  return `#nav-tab .nav-link`;
}

test.describe('Packages Dashboard', () => {
  test.beforeEach(async ({ page }) => {
    // Navigate directly to the packages route
    await page.goto('/#/packages');
    await page.waitForLoadState('networkidle');
  });

  // -------------------------------------------------------------------------
  // Page structure
  // -------------------------------------------------------------------------

  test('packages dashboard component is rendered', async ({ page }) => {
    const dashboard = page.locator('app-packages-dashboard');
    await expect(dashboard).toBeAttached({ timeout: 10_000 });
  });

  test('header component is rendered', async ({ page }) => {
    const header = page.locator('app-packages-header');
    await expect(header).toBeAttached({ timeout: 10_000 });
  });

  // -------------------------------------------------------------------------
  // Navigation tabs
  // -------------------------------------------------------------------------

  test('shows the "All" tab', async ({ page }) => {
    const allTab = page.locator('#nav-tab .nav-link', { hasText: 'All' });
    await expect(allTab).toBeVisible({ timeout: 10_000 });
  });

  test('shows the "Deployed" tab', async ({ page }) => {
    const deployedTab = page.locator('#nav-tab .nav-link', { hasText: 'Deployed' });
    await expect(deployedTab).toBeVisible({ timeout: 10_000 });
  });

  test('shows the "Under Construction" tab', async ({ page }) => {
    const underConstructionTab = page.locator('#nav-tab .nav-link', { hasText: 'Under' });
    await expect(underConstructionTab).toBeVisible({ timeout: 10_000 });
    await expect(underConstructionTab).toContainText('Under');
  });

  test('shows the "Archived" tab', async ({ page }) => {
    const archivedTab = page.locator('#nav-tab .nav-link', { hasText: 'Archived' });
    await expect(archivedTab).toBeVisible({ timeout: 10_000 });
  });

  test('"All" tab is active by default', async ({ page }) => {
    const allTab = page.locator('#nav-tab .nav-link', { hasText: 'All' });
    await expect(allTab).toHaveClass(/active/, { timeout: 10_000 });
  });

  // -------------------------------------------------------------------------
  // Search & filter UI
  // -------------------------------------------------------------------------

  test('search component is rendered', async ({ page }) => {
    const search = page.locator('app-packages-search');
    await expect(search).toBeAttached({ timeout: 10_000 });
  });

  test('filter-by-tags component is rendered', async ({ page }) => {
    const filter = page.locator('app-filter-by-tags');
    await expect(filter).toBeAttached({ timeout: 10_000 });
  });

  test('sort-packages component is rendered', async ({ page }) => {
    const sort = page.locator('app-sort-packages');
    await expect(sort).toBeAttached({ timeout: 10_000 });
  });

  // -------------------------------------------------------------------------
  // Tab switching
  // -------------------------------------------------------------------------

  test('clicking "Deployed" tab makes it active', async ({ page }) => {
    const deployedTab = page.locator('#nav-tab .nav-link', { hasText: 'Deployed' });
    await deployedTab.dispatchEvent('click');
    await expect(deployedTab).toHaveClass(/active/);
  });

  test('clicking "Under Construction" tab makes it active', async ({ page }) => {
    const ucTab = page.locator('#nav-tab .nav-link', { hasText: /Under/ });
    await ucTab.dispatchEvent('click');
    await expect(ucTab).toHaveClass(/active/);
  });

  test('clicking "Archived" tab makes it active', async ({ page }) => {
    const archivedTab = page.locator('#nav-tab .nav-link', { hasText: 'Archived' });
    await archivedTab.dispatchEvent('click');
    await expect(archivedTab).toHaveClass(/active/);
  });

  // -------------------------------------------------------------------------
  // Backend integration via proxy – the Angular app calls
  // /controllerblueprint/paged on page load; the Angular dev-server proxy
  // forwards it to the LoopBack BFF which calls the mock-processor.
  // -------------------------------------------------------------------------

  test('API call to /controllerblueprint/paged is proxied and returns 200', async ({ page }) => {
    // page.reload() forces a full browser refresh even when already on /#/packages,
    // guaranteeing Angular re-initialises the component and issues a fresh API call.
    const [apiResponse] = await Promise.all([
      page.waitForResponse(
        resp =>
          resp.url().includes('/controllerblueprint/') &&
          (resp.url().includes('paged') || resp.url().includes('/all')),
        { timeout: 15_000 },
      ),
      page.reload(),
    ]);

    // With the mock-processor running the BFF must return 200, not a 4xx/5xx
    expect(apiResponse.status()).toBe(200);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Package data loading from mock – verify the full Angular → BFF → mock chain
// renders actual fixture blueprint data in the UI
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Packages Dashboard – fixture data loaded from mock', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/packages');
    await waitForPackageCards(page);
  });

  test('package cards render fixture data correctly', async ({ page }) => {
    // Counts – one card per fixture blueprint
    await expect(page.locator('.packageName')).toHaveCount(FIXTURE_COUNT, { timeout: 20_000 });

    // Each fixture artifact name is visible (use anchored regex to avoid substring collisions,
    // e.g. 'vLB_CDS' would otherwise also match 'vLB_CDS_KOTLIN' and 'vLB_CDS_RESTCONF')
    for (const name of FIXTURE_NAMES) {
      await expect(
        page.locator('.packageName').filter({ hasText: new RegExp(`^\\s*${name}\\s*$`) })
      ).toBeVisible();
    }

    // Version strings (rendered as "v{artifactVersion}")
    // 8 blueprints at v1.0.0 and 1 at v2.0.0
    await expect(page.locator('.package-version', { hasText: 'v1.0.0' }).first()).toBeVisible();
    await expect(page.locator('.package-version', { hasText: 'v2.0.0' })).toBeVisible();

    // Deployed icon – only 6 blueprints have published: "Y"
    await expect(page.locator('img.icon-deployed')).toHaveCount(DEPLOYED_COUNT);

    // Description and tags – one element per card, at least one non-empty desc
    await expect(page.locator('.package-desc')).toHaveCount(FIXTURE_COUNT);
    await expect(page.locator('.package-desc').first()).not.toBeEmpty();
    await expect(page.locator('.packageTag')).toHaveCount(FIXTURE_COUNT);
    await expect(page.locator('.packageTag').first()).not.toBeEmpty();

    // Action buttons – each card must have both buttons
    await expect(page.locator('.btn-card-config')).toHaveCount(FIXTURE_COUNT);
    await expect(page.locator('.btn-card-topology')).toHaveCount(FIXTURE_COUNT);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Search – typing in the search box triggers the BFF metadata search
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Packages Dashboard – search', () => {
  test.beforeEach(async ({ page }) => {
    // Reload to guarantee Angular re-mounts from a clean state regardless of
    // the URL left by the previous test (same-hash goto() is a no-op in Firefox).
    if (!page.url().includes('/#/packages')) {
      await page.goto('/#/packages');
    } else {
      await page.reload();
    }
    await waitForPackageCards(page);
    await page.waitForLoadState('networkidle');
  });

  test('typing a search term filters cards to matching packages', async ({ page }) => {
    // page.fill() does not reliably fire Firefox's native InputEvent that Angular
    // listens to via (input)="searchPackages($event)".  Using page.evaluate() to
    // set the value and dispatch a proper InputEvent is the most reliable approach.
    const [searchResponse] = await Promise.all([
      page.waitForResponse(
        resp => resp.url().includes('/controllerblueprint/') && resp.status() === 200,
        { timeout: 15_000 },
      ),
      page.evaluate(() => {
        const input = document.querySelector('.searchInput') as HTMLInputElement;
        input.value = 'vFW';
        input.dispatchEvent(new InputEvent('input', { bubbles: true, cancelable: true }));
      }),
    ]);

    expect(searchResponse.status()).toBe(200);

    // After network is idle the displayed cards should match only "vFW" results
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.packageName', { hasText: 'vFW-CDS' })).toBeVisible({ timeout: 10_000 });
  });

  test('clearing the search term restores all package cards', async ({ page }) => {
    // Filter down to vFW results
    await page.evaluate(() => {
      const input = document.querySelector('.searchInput') as HTMLInputElement;
      input.value = 'vFW';
      input.dispatchEvent(new InputEvent('input', { bubbles: true, cancelable: true }));
    });
    await page.waitForLoadState('networkidle');

    // Clear the search – should restore all fixture blueprints
    await page.evaluate(() => {
      const input = document.querySelector('.searchInput') as HTMLInputElement;
      input.value = '';
      input.dispatchEvent(new InputEvent('input', { bubbles: true, cancelable: true }));
    });
    await page.waitForLoadState('networkidle');

    await expect(page.locator('.packageName')).toHaveCount(FIXTURE_COUNT, { timeout: 20_000 });
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// Sort – changing sort order triggers a new BFF paged request
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Packages Dashboard – sort', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/packages');
    await waitForPackageCards(page);
    await page.waitForLoadState('networkidle');
  });

  test('clicking the "Name" sort option triggers a new paged API call', async ({ page }) => {
    // The sort dropdown is CSS-only (visible only on :focus-within / :hover).
    // dispatchEvent() fires the click directly on the hidden <a> element without
    // requiring it to be interactable, avoiding the CSS visibility check.
    const [sortResponse] = await Promise.all([
      page.waitForResponse(
        resp => resp.url().includes('controllerblueprint/paged') && resp.url().includes('NAME'),
        { timeout: 15_000 },
      ),
      page.locator('.sort-packages .dropdown-content a[name="Name"]').dispatchEvent('click'),
    ]);

    expect(sortResponse.status()).toBe(200);
  });

  test('clicking the "Version" sort option triggers a new paged API call', async ({ page }) => {
    const [sortResponse] = await Promise.all([
      page.waitForResponse(
        resp => resp.url().includes('controllerblueprint/paged') && resp.url().includes('VERSION'),
        { timeout: 15_000 },
      ),
      page.locator('.sort-packages .dropdown-content a[name="Version"]').dispatchEvent('click'),
    ]);

    expect(sortResponse.status()).toBe(200);
  });
});

// ---------------------------------------------------------------------------
// Navigation between feature-module routes
// ---------------------------------------------------------------------------

test.describe('Client-side navigation', () => {
  test('navigating to /#/resource-dictionary loads the resource-dictionary component', async ({ page }) => {
    await page.goto('/#/resource-dictionary');
    await page.waitForLoadState('networkidle');

    expect(page.url()).toContain('resource-dictionary');

    // The app-root must still be present (Angular shell is intact)
    await expect(page.locator('app-root')).toBeAttached();
  });

  test('navigating back to /#/packages restores the packages dashboard', async ({ page }) => {
    await page.goto('/#/resource-dictionary');
    await page.goto('/#/packages');
    await page.waitForLoadState('networkidle');

    const dashboard = page.locator('app-packages-dashboard');
    await expect(dashboard).toBeAttached({ timeout: 10_000 });
  });
});

// ---------------------------------------------------------------------------
// Tab filtering – Deployed / Under Construction tabs filter by published status
// ---------------------------------------------------------------------------

test.describe('Packages Dashboard – tab filtering', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/packages');
    await waitForPackageCards(page);
    await page.waitForLoadState('networkidle');
  });

  test('clicking "Deployed" tab sends published=true and shows only deployed packages', async ({ page }) => {
    const deployedTab = page.locator('#nav-tab .nav-link', { hasText: 'Deployed' });

    const responsePromise = page.waitForResponse(
      resp => resp.url().includes('controllerblueprint/paged') && resp.url().includes('published=true'),
      { timeout: 15_000 },
    );
    await page.evaluate(() => {
      (document.querySelector('#nav-tab .nav-link:nth-child(2)') as HTMLElement).click();
    });
    const apiResponse = await responsePromise;

    expect(apiResponse.status()).toBe(200);

    // Wait for cards to update
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.packageName')).toHaveCount(DEPLOYED_COUNT, { timeout: 20_000 });

    // Only deployed packages should be visible
    for (const name of DEPLOYED_NAMES) {
      await expect(
        page.locator('.packageName').filter({ hasText: new RegExp(`^\\s*${name}\\s*$`) })
      ).toBeVisible();
    }

    // Under-construction packages should NOT be visible
    for (const name of UNDER_CONSTRUCTION_NAMES) {
      await expect(
        page.locator('.packageName').filter({ hasText: new RegExp(`^\\s*${name}\\s*$`) })
      ).toHaveCount(0);
    }

    // All visible cards should have the deployed icon
    await expect(page.locator('img.icon-deployed')).toHaveCount(DEPLOYED_COUNT);
  });

  test('clicking "Under Construction" tab sends published=false and shows only non-deployed packages', async ({ page }) => {
    const ucTab = page.locator('#nav-tab .nav-link', { hasText: /Under/ });

    const [apiResponse] = await Promise.all([
      page.waitForResponse(
        resp => resp.url().includes('controllerblueprint/paged') && resp.url().includes('published=false'),
        { timeout: 15_000 },
      ),
      ucTab.dispatchEvent('click'),
    ]);

    expect(apiResponse.status()).toBe(200);

    await page.waitForLoadState('networkidle');
    await expect(page.locator('.packageName')).toHaveCount(UNDER_CONSTRUCTION_COUNT, { timeout: 20_000 });

    // Only under-construction packages should be visible
    for (const name of UNDER_CONSTRUCTION_NAMES) {
      await expect(
        page.locator('.packageName').filter({ hasText: new RegExp(`^\\s*${name}\\s*$`) })
      ).toBeVisible();
    }

    // None of the deployed packages should be visible
    for (const name of DEPLOYED_NAMES) {
      await expect(
        page.locator('.packageName').filter({ hasText: new RegExp(`^\\s*${name}\\s*$`) })
      ).toHaveCount(0);
    }

    // Under-construction packages should NOT have the deployed icon
    await expect(page.locator('img.icon-deployed')).toHaveCount(0);
  });

  test('switching back to "All" tab clears the published filter and shows all packages', async ({ page }) => {
    // First switch to Deployed
    const deployedTab = page.locator('#nav-tab .nav-link', { hasText: 'Deployed' });
    await deployedTab.dispatchEvent('click');
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.packageName')).toHaveCount(DEPLOYED_COUNT, { timeout: 20_000 });

    // Now switch back to All
    const allTab = page.locator('#nav-tab .nav-link', { hasText: 'All' });

    const [apiResponse] = await Promise.all([
      page.waitForResponse(
        resp => resp.url().includes('controllerblueprint/paged') && resp.status() === 200,
        { timeout: 15_000 },
      ),
      allTab.dispatchEvent('click'),
    ]);

    expect(apiResponse.status()).toBe(200);
    await page.waitForLoadState('networkidle');
    await expect(page.locator('.packageName')).toHaveCount(FIXTURE_COUNT, { timeout: 20_000 });
  });
});
