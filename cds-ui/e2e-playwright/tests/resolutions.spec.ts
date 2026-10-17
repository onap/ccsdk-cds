/*
 * resolutions.spec.ts
 *
 * End-to-end tests for the Resolutions page, which shows what executions
 * left behind in the blueprints processor:
 *   1. Navigation – the sidebar link opens the page.
 *   2. Resolved resources – lookup by resolution key or by resource type and
 *      ID lists the values of the latest occurrence and its rendered template;
 *      older occurrences can be selected.
 *   3. Empty results – unknown keys say that nothing is stored.
 *   4. Config snapshots – running and candidate are shown side by side, and
 *      the resources of a type can be listed and opened.
 */

import { test, expect, Page } from '@playwright/test';

async function openResolutions(page: Page) {
    await page.goto('/#/resolutions');
    await expect(page.getByRole('heading', { level: 1, name: 'Resolutions' })).toBeVisible({ timeout: 20_000 });
}

async function selectBlueprint(page: Page, name: string) {
    const select = page.getByLabel('Blueprint name');
    await expect(select.locator('option', { hasText: name })).toBeAttached({ timeout: 20_000 });
    await select.selectOption(name);
    await expect(page.getByLabel('Blueprint version')).toHaveValue('1.0.0');
}

test.describe('Resolutions page', () => {
    test('is reachable from the main navigation', async ({ page }) => {
        await page.goto('/#/packages');
        const nav = page.getByRole('navigation', { name: 'Main' });
        await nav.getByRole('link', { name: 'Resolutions' }).click();
        await expect(page.getByRole('heading', { level: 1, name: 'Resolutions' })).toBeVisible({ timeout: 20_000 });
        await expect(nav.getByRole('link', { name: 'Resolutions' })).toHaveAttribute('aria-current', 'page');
    });

    test('looks up resolved resources and the rendered template by resolution key', async ({ page }) => {
        await openResolutions(page);
        await selectBlueprint(page, 'vFW-CDS');
        await page.getByLabel('Artifact name').fill('baseconfig');
        await page.getByRole('textbox', { name: 'Resolution key' }).fill('vfw-key-1');
        await page.getByRole('button', { name: 'Look up' }).click();

        const table = page.getByRole('table', { name: /Resolved values/ });
        await expect(table).toBeVisible();
        await expect(page.getByLabel('Occurrence')).toHaveValue(/2/);
        await expect(table.getByRole('row')).toHaveCount(3);
        await expect(table.getByRole('row', { name: /hostname/ })).toContainText('vfw-01');
        await expect(page.getByLabel('Rendered template content')).toHaveText('hostname vfw-01\nip-address 10.0.0.1');

        await page.getByLabel('Occurrence').selectOption({ label: '1' });
        await expect(table.getByRole('row', { name: /hostname/ })).toContainText('vfw-old');
        await expect(page.getByLabel('Rendered template content')).toHaveText('hostname vfw-old');
    });

    test('looks up by resource type and ID', async ({ page }) => {
        await openResolutions(page);
        await selectBlueprint(page, 'vFW-CDS');
        await page.getByLabel('Artifact name').fill('baseconfig');
        await page.getByLabel('Resource type and ID').check();
        await page.locator('#resolutionResourceType').fill('vnf');
        await page.locator('#resolutionResourceId').fill('vfw-vnf-1');
        await page.getByRole('button', { name: 'Look up' }).click();

        await expect(page.getByRole('table', { name: /Resolved values/ }).getByRole('row')).toHaveCount(3);
        await expect(page.getByLabel('Rendered template content')).toContainText('vfw-01');
    });

    test('says when nothing is stored for the keys', async ({ page }) => {
        await openResolutions(page);
        await selectBlueprint(page, 'vFW-CDS');
        await page.getByLabel('Artifact name').fill('baseconfig');
        await page.getByRole('textbox', { name: 'Resolution key' }).fill('unknown-key');
        await page.getByRole('button', { name: 'Look up' }).click();

        await expect(page.getByText('No resolved resources are stored for these keys.')).toBeVisible();
        await expect(page.getByText('No rendered template is stored for these keys.')).toBeVisible();
    });

    test('shows running and candidate config snapshots side by side', async ({ page }) => {
        await openResolutions(page);
        await page.locator('#snapshotResourceType').fill('PNF');
        await page.locator('#snapshotResourceId').fill('pnf-1');
        await page.getByRole('button', { name: 'Show snapshots' }).click();

        await expect(page.getByLabel('Running configuration')).toHaveText('<config><mtu>1500</mtu></config>');
        await expect(page.getByLabel('Candidate configuration')).toHaveText('<config><mtu>9000</mtu></config>');
    });

    test('lists the resources of a type and opens one', async ({ page }) => {
        await openResolutions(page);
        await page.locator('#snapshotResourceType').fill('PNF');
        await page.getByRole('button', { name: 'List resources of this type' }).click();

        const table = page.getByRole('table', { name: 'Snapshots stored for resource type PNF' });
        await expect(table.getByRole('row')).toHaveCount(4);
        await table.getByRole('button', { name: 'Show snapshots of pnf-2' }).click();

        await expect(page.getByLabel('Running configuration')).toHaveText('<config><mtu>1400</mtu></config>');
        await expect(page.getByText('No candidate snapshot is stored.')).toBeVisible();
    });
});
