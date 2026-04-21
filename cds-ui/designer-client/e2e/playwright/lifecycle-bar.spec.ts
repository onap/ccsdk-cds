import { test, expect, Page } from '@playwright/test';

async function mockApis(page: Page) {
    await page.route('/controllerblueprint/**', route =>
        route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
    await page.route('/resourcedictionary/**', route =>
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));
}

test.describe('Lifecycle action bar', () => {
    test.beforeEach(async ({ page }) => {
        await mockApis(page);
        await page.goto('/#/packages/createPackage');
        await page.waitForSelector('.lifecycle-bar', { timeout: 20000 });
    });

    test('renders 4 lifecycle steps', async ({ page }) => {
        const steps = page.locator('.lifecycle-step');
        await expect(steps).toHaveCount(4);
    });

    test('step labels match the CBA workflow', async ({ page }) => {
        const labels = page.locator('.step-label');
        await expect(labels.nth(0)).toContainText('Edit');
        await expect(labels.nth(1)).toContainText('Data Dictionary');
        await expect(labels.nth(2)).toContainText('Enrich');
        await expect(labels.nth(3)).toContainText('Publish');
    });

    test('step 1 (Edit) has complete class on initial load', async ({ page }) => {
        await expect(page.locator('.lifecycle-step').first()).toHaveClass(/complete/);
    });

    test('steps 2–4 are not complete on initial load', async ({ page }) => {
        const steps = page.locator('.lifecycle-step');
        for (let i = 1; i <= 3; i++) {
            await expect(steps.nth(i)).not.toHaveClass(/complete/);
        }
    });

    test('Import DD button is present in step 2', async ({ page }) => {
        const importBtn = page.locator('.lifecycle-step').nth(1).locator('.lifecycle-btn');
        await expect(importBtn).toBeVisible();
        await expect(importBtn).toContainText('Import DD');
    });

    test('clicking Import DD button opens the DD import modal', async ({ page }) => {
        const importBtn = page.locator('.lifecycle-step').nth(1).locator('.lifecycle-btn');
        await importBtn.click();
        await expect(page.locator('#ddImportModal')).toBeVisible({ timeout: 3000 });
        await expect(page.locator('#ddImportModal .modal-title')).toContainText('Import Data Dictionary');
    });

    test('DD import modal has file picker and Import button', async ({ page }) => {
        await page.locator('.lifecycle-step').nth(1).locator('.lifecycle-btn').click();
        await expect(page.locator('#ddImportModal')).toBeVisible();
        // Import button starts disabled (no file selected)
        const importSubmitBtn = page.locator('#ddImportModal .modal-footer .btn-primary');
        await expect(importSubmitBtn).toBeDisabled();
    });

    test('DD import modal closes on Cancel', async ({ page }) => {
        await page.locator('.lifecycle-step').nth(1).locator('.lifecycle-btn').click();
        await expect(page.locator('#ddImportModal')).toBeVisible();
        await page.locator('#ddImportModal .btn-secondary').click();
        await expect(page.locator('#ddImportModal')).not.toBeVisible({ timeout: 3000 });
    });

    test('Enrich button is disabled when package metadata is not filled', async ({ page }) => {
        const enrichBtn = page.locator('.lifecycle-step').nth(2).locator('.lifecycle-btn');
        await expect(enrichBtn).toBeDisabled();
    });

    test('Publish button is disabled when not yet enriched', async ({ page }) => {
        const publishBtn = page.locator('.lifecycle-step').nth(3).locator('.lifecycle-btn');
        await expect(publishBtn).toBeDisabled();
    });

    test('step circles are numbered 1–4', async ({ page }) => {
        const circles = page.locator('.step-circle');
        for (let i = 0; i < 4; i++) {
            await expect(circles.nth(i)).toContainText(String(i + 1));
        }
    });

    test('DD import succeeds end-to-end via mocked API', async ({ page }) => {
        // Override API to accept the DD upload
        await page.route('/resourcedictionary/definition-bulk', route =>
            route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));

        // Open modal
        await page.locator('.lifecycle-step').nth(1).locator('.lifecycle-btn').click();
        await expect(page.locator('#ddImportModal')).toBeVisible();

        // Upload a valid dd.json via the file drop zone
        const ddContent = JSON.stringify([{ name: 'stream-count', tags: 'test' }]);
        const ddFile = Buffer.from(ddContent, 'utf-8');

        await page.locator('#ddImportModal').locator('input[type="file"]').setInputFiles({
            name: 'dd.json',
            mimeType: 'application/json',
            buffer: ddFile,
        });

        // Selected file name should appear
        await expect(page.locator('#ddImportModal .modal-body strong')).toContainText('dd.json');

        // Click Import
        await page.locator('#ddImportModal .modal-footer .btn-primary').click();

        // Step 2 should become complete
        await expect(page.locator('.lifecycle-step').nth(1)).toHaveClass(/complete/, { timeout: 5000 });
    });
});
