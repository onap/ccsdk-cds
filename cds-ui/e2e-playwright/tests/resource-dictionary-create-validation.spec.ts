/*
 * resource-dictionary-create-validation.spec.ts
 *
 * End-to-end tests for the Resource Dictionary creation form validation.
 *
 * These tests verify:
 *   1. Save button is disabled when required fields are empty.
 *   2. Required field indicators are shown (Name, Data Type, Description, Updated By).
 *   3. Save button becomes enabled once all required fields are filled.
 *   4. Backend returns 400 Bad Request when required fields are missing.
 */

import { test, expect } from '@playwright/test';

test.describe('Resource Dictionary – create form validation', () => {
    test.beforeEach(async ({ page }) => {
        await page.goto('/#/resource-dictionary/createDictionary');
        await page.waitForLoadState('networkidle');
    });

    test('save button is disabled when no fields are filled', async ({ page }) => {
        const saveButton = page.locator('button.action-button.save');
        await expect(saveButton).toBeVisible({ timeout: 10_000 });
        await expect(saveButton).toBeDisabled();
    });

    test('required field indicators are shown for Name, Data Type, Description, Updated By', async ({ page }) => {
        const metadata = page.locator('app-dictionary-metadata');
        await expect(metadata).toBeAttached({ timeout: 10_000 });

        const requiredIndicators = metadata.locator('.required-indicator');
        await expect(requiredIndicators).toHaveCount(4);
    });

    test('save button remains disabled when only some required fields are filled', async ({ page }) => {
        const metadata = page.locator('app-dictionary-metadata');
        await expect(metadata).toBeAttached({ timeout: 10_000 });

        // Fill only the Name field
        const nameInput = metadata.locator('input[placeholder="Dictionary Name"]');
        await nameInput.fill('test-dictionary');

        const saveButton = page.locator('button.action-button.save');
        await expect(saveButton).toBeDisabled();
    });

    test('save button becomes enabled when all required fields are filled', async ({ page }) => {
        const metadata = page.locator('app-dictionary-metadata');
        await expect(metadata).toBeAttached({ timeout: 10_000 });

        // Fill all required fields
        await metadata.locator('input[placeholder="Dictionary Name"]').fill('test-dictionary');
        await metadata.locator('input[placeholder="Data Type"]').fill('string');
        await metadata.locator('input[placeholder="Describe the package"]').fill('A test dictionary');
        await metadata.locator('input[placeholder="Updated By"]').fill('test-user@example.com');

        const saveButton = page.locator('button.action-button.save');
        await expect(saveButton).toBeEnabled({ timeout: 5_000 });
    });

    test('save button becomes disabled again when a required field is cleared', async ({ page }) => {
        const metadata = page.locator('app-dictionary-metadata');
        await expect(metadata).toBeAttached({ timeout: 10_000 });

        // Fill all required fields
        await metadata.locator('input[placeholder="Dictionary Name"]').fill('test-dictionary');
        await metadata.locator('input[placeholder="Data Type"]').fill('string');
        await metadata.locator('input[placeholder="Describe the package"]').fill('A test dictionary');
        await metadata.locator('input[placeholder="Updated By"]').fill('test-user@example.com');

        const saveButton = page.locator('button.action-button.save');
        await expect(saveButton).toBeEnabled({ timeout: 5_000 });

        // Clear the Name field
        await metadata.locator('input[placeholder="Dictionary Name"]').fill('');

        await expect(saveButton).toBeDisabled({ timeout: 5_000 });
    });
});

test.describe('Resource Dictionary – backend validation via mock', () => {
    test('POST /api/v1/dictionary/definition with empty body returns 400', async ({ request }) => {
        const resp = await request.post('http://localhost:8080/api/v1/dictionary/definition', {
            data: {},
            headers: { 'Content-Type': 'application/json' },
        });
        expect(resp.status()).toBe(400);
        const body = await resp.json();
        expect(body.message).toContain('name is missing');
    });

    test('POST /api/v1/dictionary/definition with partial body returns 400', async ({ request }) => {
        const resp = await request.post('http://localhost:8080/api/v1/dictionary/definition', {
            data: {
                name: 'test-dict',
                property: { type: '', description: '' },
                'updated-by': '',
            },
            headers: { 'Content-Type': 'application/json' },
        });
        expect(resp.status()).toBe(400);
        const body = await resp.json();
        expect(body.message).toContain('description is missing');
    });

    test('POST /api/v1/dictionary/definition with valid body returns 200', async ({ request }) => {
        const resp = await request.post('http://localhost:8080/api/v1/dictionary/definition', {
            data: {
                name: 'test-dict',
                property: { type: 'string', description: 'A test dictionary' },
                'updated-by': 'test-user@example.com',
            },
            headers: { 'Content-Type': 'application/json' },
        });
        expect(resp.status()).toBe(200);
    });
});
