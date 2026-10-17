/*
 * ============LICENSE_START=======================================================
 * ONAP : CDS
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import { test, expect } from '@playwright/test';

test.describe('Resource Dictionary – existing entries', () => {
    test('a dictionary card opens the entry for editing', async ({ page }) => {
        await page.goto('/#/resource-dictionary');
        const card = page.locator('app-dictionary-list a.card-title').first();
        await expect(card).toBeVisible({ timeout: 15_000 });
        const name = (await card.textContent()).trim();

        await card.click();

        await expect(page).toHaveURL(new RegExp(`#/resource-dictionary/edit/${encodeURIComponent(name)}$`));
        await expect(page.getByRole('heading', { level: 1 })).toHaveText(name);
        const nameInput = page.getByPlaceholder('Dictionary Name');
        await expect(nameInput).toHaveValue(name);
        await expect(nameInput).not.toBeEditable();
    });

    test('an entry can be edited and deleted', async ({ page, request }) => {
        const name = 'e2e-edit-delete';
        const seeded = await request.post('/resourcedictionary/definition', {
            data: {
                name,
                tags: 'e2e',
                'updated-by': 'e2e',
                property: { type: 'string', description: 'before edit' },
                sources: { input: { type: 'source-input' } },
            },
        });
        expect(seeded.ok()).toBe(true);

        await page.goto(`/#/resource-dictionary/edit/${name}`);
        const description = page.getByPlaceholder('Describe the package');
        await expect(description).toHaveValue('before edit', { timeout: 15_000 });

        await description.fill('after edit');
        const [saved] = await Promise.all([
            page.waitForResponse(r => r.url().endsWith('/resourcedictionary/definition')),
            page.getByRole('button', { name: 'Save' }).click(),
        ]);
        expect(saved.ok()).toBe(true);
        const stored = await (await request.get(`/resourcedictionary/${name}`)).json();
        expect(stored.definition.property.description).toBe('after edit');
        expect(stored.definition.sources).toEqual({ input: { type: 'source-input' } });

        await page.getByRole('button', { name: 'Delete' }).click();
        const dialog = page.getByRole('dialog', { name: 'Delete Dictionary' });
        await expect(dialog).toBeVisible();
        const [deleted] = await Promise.all([
            page.waitForResponse(r => r.request().method() === 'DELETE'),
            dialog.getByRole('button', { name: 'Delete' }).click(),
        ]);
        expect(deleted.ok()).toBe(true);

        await expect(page).toHaveURL(/#\/resource-dictionary$/);
        expect((await request.get(`/resourcedictionary/${name}`)).ok()).toBe(false);
    });
});
