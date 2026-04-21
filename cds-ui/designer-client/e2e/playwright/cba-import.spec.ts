import { test, expect, Page } from '@playwright/test';
import * as path from 'path';
import * as fs from 'fs';
import * as os from 'os';

async function mockApis(page: Page) {
    await page.route('/controllerblueprint/**', route =>
        route.fulfill({
            status: 200, contentType: 'application/json',
            body: JSON.stringify({ content: [], totalElements: 0, totalPages: 1, first: true, last: true })
        }));
    await page.route('/resourcedictionary/**', route =>
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));
}

test.describe('Edit CBA ZIP button', () => {
    test.beforeEach(async ({ page }) => {
        await mockApis(page);
        await page.goto('/#/packages');
        await page.waitForSelector('.addPaackage-card', { timeout: 15000 });
    });

    test('Edit CBA ZIP button is visible in the package add card', async ({ page }) => {
        await expect(page.locator('text=Edit CBA ZIP')).toBeVisible();
    });

    test('Edit CBA ZIP button has a hidden file input accepting .zip', async ({ page }) => {
        const fileInput = page.locator('.addPaackage-card input[type="file"][accept=".zip"]');
        await expect(fileInput).toBeAttached();
    });

    test('uploading a valid CBA zip navigates to the creation screen', async ({ page }) => {
        // Build a minimal valid CBA zip in memory
        // PKZip end-of-central-directory record (empty archive)
        const emptyZipBytes = Buffer.from([
            0x50, 0x4b, 0x05, 0x06,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00
        ]);

        const navPromise = page.waitForURL(/createPackage/, { timeout: 8000 });
        await page.locator('.addPaackage-card input[type="file"][accept=".zip"]').setInputFiles({
            name: 'test-cba.zip',
            mimeType: 'application/zip',
            buffer: emptyZipBytes,
        });
        await navPromise;
        await expect(page).toHaveURL(/createPackage/);
    });

    test('uploading a corrupt file shows an error toast', async ({ page }) => {
        await page.locator('.addPaackage-card input[type="file"][accept=".zip"]').setInputFiles({
            name: 'corrupt.zip',
            mimeType: 'application/zip',
            buffer: Buffer.from('not a zip file at all'),
        });
        // Wait for the async error handler
        await page.waitForTimeout(300);
        await expect(page.locator('.toast-error')).toBeVisible({ timeout: 5000 });
    });
});
