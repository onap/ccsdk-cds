/*
 * execution.spec.ts
 *
 * End-to-end tests for the Blueprint Execution page.  These tests verify:
 *   1. Page structure – execution dashboard component renders with tabs.
 *   2. Navigation – the Execute link in the sidebar navigates to the page.
 *   3. Tab switching – Setup / History / Live View tabs work correctly.
 *   4. Execution Setup – form fields render, blueprint dropdown is populated
 *      from the mock-processor, and the JSON payload editor is visible.
 *   5. Action selection – selecting a blueprint+version populates the action
 *      dropdown from the blueprint's workflows.
 *   6. Execution API – submitting the form POSTs to the execution endpoint
 *      and the mock-processor returns a successful response.
 *   7. Package detail integration – the Execute button on the package detail
 *      page navigates to the execution page with pre-filled parameters.
 */

import { test, expect } from '@playwright/test';

// ── helpers ────────────────────────────────────────────────────────────────────

/** Wait for the execution dashboard to be rendered. */
async function waitForExecutionDashboard(page: import('@playwright/test').Page) {
    await expect(page.locator('app-execution-dashboard')).toBeAttached({ timeout: 20_000 });
}

/** Wait for the blueprint select dropdown to be populated from mock data. */
async function waitForBlueprintOptions(page: import('@playwright/test').Page) {
    await expect(page.locator('#blueprintName option').nth(1)).toBeAttached({ timeout: 20_000 });
}

// ─────────────────────────────────────────────────────────────────────────────
// Page structure
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Execution Page – structure', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/#/execute');
        await page.waitForLoadState('networkidle');
    });

    test('execution dashboard component is rendered', async ({ page }) => {
        await waitForExecutionDashboard(page);
    });

    test('breadcrumb shows Execute', async ({ page }) => {
        await waitForExecutionDashboard(page);
        const breadcrumb = page.locator('.breadcrumb-header');
        await expect(breadcrumb).toContainText('Execute');
    });

    test('shows the Execution Setup tab', async ({ page }) => {
        await waitForExecutionDashboard(page);
        const tab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Execution Setup' });
        await expect(tab).toBeVisible({ timeout: 10_000 });
    });

    test('shows the Execution History tab', async ({ page }) => {
        await waitForExecutionDashboard(page);
        const tab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Execution History' });
        await expect(tab).toBeVisible({ timeout: 10_000 });
    });

    test('shows the Live View tab', async ({ page }) => {
        await waitForExecutionDashboard(page);
        const tab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Live View' });
        await expect(tab).toBeVisible({ timeout: 10_000 });
    });

    test('Execution Setup tab is active by default', async ({ page }) => {
        await waitForExecutionDashboard(page);
        const tab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Execution Setup' });
        await expect(tab).toHaveClass(/active/, { timeout: 10_000 });
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Tab switching
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Execution Page – tab switching', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/#/execute');
        await waitForExecutionDashboard(page);
    });

    test('clicking History tab shows history content', async ({ page }) => {
        const historyTab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Execution History' });
        await historyTab.click();
        await expect(historyTab).toHaveClass(/active/);
        await expect(page.locator('app-execution-history')).toBeAttached();
    });

    test('clicking Live View tab shows live view content', async ({ page }) => {
        const liveTab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Live View' });
        await liveTab.click();
        await expect(liveTab).toHaveClass(/active/);
        await expect(page.locator('app-live-view')).toBeAttached();
    });

    test('clicking back to Setup tab re-renders setup form', async ({ page }) => {
        // Switch away first
        const historyTab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Execution History' });
        await historyTab.click();
        await expect(historyTab).toHaveClass(/active/);

        // Switch back
        const setupTab = page.locator('#execution-nav-tab .nav-link', { hasText: 'Execution Setup' });
        await setupTab.click();
        await expect(setupTab).toHaveClass(/active/);
        await expect(page.locator('app-execution-setup')).toBeAttached();
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Execution Setup – form elements
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Execution Page – setup form', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/#/execute');
        await waitForExecutionDashboard(page);
    });

    test('blueprint name dropdown is rendered', async ({ page }) => {
        const select = page.locator('#blueprintName');
        await expect(select).toBeVisible({ timeout: 10_000 });
    });

    test('blueprint version dropdown is rendered', async ({ page }) => {
        const select = page.locator('#blueprintVersion');
        await expect(select).toBeVisible({ timeout: 10_000 });
    });

    test('action name dropdown is rendered', async ({ page }) => {
        const select = page.locator('#actionName');
        await expect(select).toBeVisible({ timeout: 10_000 });
    });

    test('Execute button is rendered and initially disabled', async ({ page }) => {
        const btn = page.locator('.btn-execute');
        await expect(btn).toBeVisible({ timeout: 10_000 });
        await expect(btn).toBeDisabled();
    });

    test('JSON payload editor is rendered', async ({ page }) => {
        // ace-editor renders a div with class "ace_editor"
        const editor = page.locator('.ace_editor');
        await expect(editor.first()).toBeVisible({ timeout: 15_000 });
    });

    test('blueprint dropdown is populated from mock API', async ({ page }) => {
        await waitForBlueprintOptions(page);
        // We expect at least some options from the fixture data
        const options = page.locator('#blueprintName option');
        const count = await options.count();
        // First option is the placeholder, rest are blueprint names
        expect(count).toBeGreaterThan(1);
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Execution API integration via mock
// ─────────────────────────────────────────────────────────────────────────────

/** Helper: select first blueprint + wait for version + wait for actions to load. */
async function selectBlueprintAndVersion(page: import('@playwright/test').Page) {
    await page.selectOption('#blueprintName', { index: 1 });
    // Wait for version dropdown to populate and auto-select
    await expect(page.locator('#blueprintVersion option').nth(1)).toBeAttached({ timeout: 10_000 });
    const versionOptions = await page.locator('#blueprintVersion option').count();
    if (versionOptions > 1) {
        await page.selectOption('#blueprintVersion', { index: 1 });
    }
    // Wait for action dropdown to populate from by-name API
    await expect(page.locator('#actionName option').nth(1)).toBeAttached({ timeout: 10_000 });
}

test.describe('Execution Page – API integration', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/#/execute');
        await waitForExecutionDashboard(page);
        await waitForBlueprintOptions(page);
    });

    test('selecting a blueprint populates the action dropdown', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        const actionOptions = page.locator('#actionName option');
        const count = await actionOptions.count();
        // placeholder + at least one real action
        expect(count).toBeGreaterThan(1);
    });

    test('selecting a blueprint enables the Execute button', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        // Select an action if not auto-selected
        const actionOptions = await page.locator('#actionName option').count();
        if (actionOptions > 1) {
            await page.selectOption('#actionName', { index: 1 });
        }
        await page.waitForTimeout(300);
        const btn = page.locator('.btn-execute');
        await expect(btn).toBeEnabled({ timeout: 5_000 });
    });

    test('executing a blueprint sends request and receives response', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        // Select an action
        const actionOptions = await page.locator('#actionName option').count();
        if (actionOptions > 1) {
            await page.selectOption('#actionName', { index: 1 });
        }
        await page.waitForTimeout(300);

        // Click execute and wait for the API round-trip
        const [apiResponse] = await Promise.all([
            page.waitForResponse(
                resp => resp.url().includes('/controllerblueprint/execute') && resp.status() === 200,
                { timeout: 15_000 },
            ),
            page.locator('.btn-execute').click(),
        ]);

        expect(apiResponse.status()).toBe(200);

        // Verify the response JSON contains expected mock data
        const body = await apiResponse.json();
        expect(body).toHaveProperty('status');
        expect(body.status.code).toBe(200);
        expect(body.status.message).toBe('success');
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Dynamic action inputs
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Execution Page – action inputs', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/#/execute');
        await waitForExecutionDashboard(page);
        await waitForBlueprintOptions(page);
    });

    test('selecting an action shows action input fields', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        // Select the first action
        await page.selectOption('#actionName', { index: 1 });
        // Wait for the workflow-spec API call to complete and form to render
        const inputsSection = page.locator('#actionInputs');
        await expect(inputsSection).toBeAttached({ timeout: 10_000 });
    });

    test('complex input group renders nested fields', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        await page.selectOption('#actionName', { index: 1 });
        // Wait for inputs section
        await expect(page.locator('#actionInputs')).toBeAttached({ timeout: 10_000 });
        // Complex group should have nested fields inside .complex-fields
        const complexFields = page.locator('.complex-fields input, .complex-fields select');
        const count = await complexFields.count();
        expect(count).toBeGreaterThan(0);
    });

    test('typing in a field updates the JSON payload', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        await page.selectOption('#actionName', { index: 1 });
        await expect(page.locator('#actionInputs')).toBeAttached({ timeout: 10_000 });

        // Find the first text input in the inputs section and type a value
        const firstInput = page.locator('#actionInputs input[type="text"]').first();
        await firstInput.fill('test-value-123');

        // Wait for payload to update
        await page.waitForTimeout(500);

        // Get the ace editor content and verify the typed value appears
        const editorContent = await page.locator('.ace_editor .ace_text-layer').first().textContent();
        expect(editorContent).toContain('test-value-123');
    });

    test('reset clears input field values', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        await page.selectOption('#actionName', { index: 1 });
        await expect(page.locator('#actionInputs')).toBeAttached({ timeout: 10_000 });

        // Fill a field
        const firstInput = page.locator('#actionInputs input[type="text"]').first();
        await firstInput.fill('will-be-cleared');
        await page.waitForTimeout(300);

        // Click reset
        await page.locator('button', { hasText: 'Reset' }).click();
        await page.waitForTimeout(300);

        // Field should be cleared
        await expect(firstInput).toHaveValue('');
    });

    test('payload JSON includes action-request structure', async ({ page }) => {
        await selectBlueprintAndVersion(page);
        // Select an action
        await page.selectOption('#actionName', { index: 1 });
        await expect(page.locator('#actionInputs')).toBeAttached({ timeout: 10_000 });
        await page.waitForTimeout(500);

        // Check that the payload contains a *-request key
        const editorContent = await page.locator('.ace_editor .ace_text-layer').first().textContent();
        expect(editorContent).toMatch(/-request/);
    });
});

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar navigation
// ─────────────────────────────────────────────────────────────────────────────

test.describe('Execution Page – navigation', () => {
    test('Execute link is visible in the sidebar', async ({ page }) => {
        await page.goto('/#/packages');
        await page.waitForLoadState('networkidle');
        const executeLink = page.locator('.menu-dropdown a', { hasText: 'Execute' });
        await expect(executeLink).toBeVisible({ timeout: 10_000 });
    });

    test('clicking Execute link in sidebar navigates to execute page', async ({ page }) => {
        await page.goto('/#/packages');
        await page.waitForLoadState('networkidle');
        const executeLink = page.locator('.menu-dropdown a', { hasText: 'Execute' });
        await executeLink.click();
        await expect(page).toHaveURL(/\/#\/execute/);
        await waitForExecutionDashboard(page);
    });
});
