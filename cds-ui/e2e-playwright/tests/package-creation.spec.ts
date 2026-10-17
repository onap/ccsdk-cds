/*
 * package-creation.spec.ts
 *
 * End-to-end tests for the lifecycle of a new package on the package
 * creation page: when Save, Enrich and Publish become available, that
 * Publish never sends an outdated enrichment, and when leaving the page
 * asks about unsaved changes.
 */

import { test, expect, Page } from '@playwright/test';

// RT-resource-resolution 1.0.0 – first entry in blueprints.json fixtures,
// which the mock processor also returns for every publish
const EXISTING_NAME = 'RT-resource-resolution';
const EXISTING_ID = '66bfe8a0-4789-4b5d-ad7f-f2157e3a2021';

function lifecycleButton(page: Page, name: string) {
  return page.locator('.lifecycle-bar').getByRole('button', { name, exact: true });
}

async function fillMetadata(page: Page, name = 'e2e-package', version = '1.0.0', tag = 'e2e') {
  await page.getByPlaceholder('Package name').fill(name);
  await page.getByPlaceholder('Example: 1.0.0').fill(version);
  if (tag) {
    const tags = page.getByPlaceholder('Ex., vDNS-CDS');
    await tags.fill(tag);
    await tags.press('Enter');
  }
  await page.getByPlaceholder('Describe the package').click();
}

async function addTemplate(page: Page, content: string) {
  await page.locator('#nav-template-tab').click();
  const templatePane = page.locator('#nav-template');
  await templatePane.getByPlaceholder('Template name').fill('hostname');
  await templatePane.locator('.ace_content').first().click();
  await page.keyboard.type(content);
  await templatePane.getByRole('button', { name: 'Finish' }).click();
  await page.locator('#nav-metadata-tab').click();
}

test.describe('Package creation lifecycle', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/#/packages/createPackage');
    await page.waitForLoadState('networkidle');
  });

  test('nothing is marked done on an empty page', async ({ page }) => {
    await expect(page.locator('.lifecycle-step').first()).not.toHaveClass(/complete/);
    await expect(page.getByRole('button', { name: 'Save' })).toBeDisabled();
    await expect(lifecycleButton(page, 'Enrich')).toBeDisabled();
  });

  test('the metadata tab has no placeholder controls', async ({ page }) => {
    await expect(page.getByText('Dictionary Library Instance')).toHaveCount(0);
    await expect(page.locator('app-metadata-tab input[type="radio"]')).toHaveCount(0);
  });

  test('Save stays disabled until a tag is added', async ({ page }) => {
    await fillMetadata(page, 'e2e-package', '1.0.0', '');
    await expect(page.getByRole('button', { name: 'Save' })).toBeDisabled();

    const tags = page.getByPlaceholder('Ex., vDNS-CDS');
    await tags.fill('e2e');
    await tags.press('Enter');

    await expect(page.getByRole('button', { name: 'Save' })).toBeEnabled();
    await expect(page.locator('.lifecycle-step').first()).toHaveClass(/complete/);
  });

  test('Save stays disabled for a name and version that already exist', async ({ page }) => {
    await fillMetadata(page, EXISTING_NAME);
    await expect(page.getByText('Package name already exists with this version.', { exact: false })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Save' })).toBeDisabled();

    await page.getByPlaceholder('Package name').fill('e2e-package');
    await expect(page.getByRole('button', { name: 'Save' })).toBeEnabled();
  });

  test('Enrich needs a template', async ({ page }) => {
    await fillMetadata(page);
    await expect(lifecycleButton(page, 'Enrich')).toBeDisabled();
    await expect(page.getByText('Add a template first')).toBeVisible();

    await addTemplate(page, 'hostname ${hostname}');

    await expect(lifecycleButton(page, 'Enrich')).toBeEnabled();
    await expect(page.getByText('Add a template first')).toHaveCount(0);
  });

  test('editing the package after Enrich requires a new Enrich', async ({ page }) => {
    await fillMetadata(page);
    await addTemplate(page, 'hostname ${hostname}');
    await lifecycleButton(page, 'Enrich').click();
    await expect(lifecycleButton(page, 'Publish')).toBeEnabled();

    const description = page.getByPlaceholder('Describe the package');
    await description.fill('changed after enrich');
    await description.press('Tab');

    await expect(lifecycleButton(page, 'Publish')).toBeDisabled();
    await expect(page.locator('.lifecycle-step').nth(2)).not.toHaveClass(/complete/);
  });

  test('Publish opens the published package without an unsaved-changes prompt', async ({ page }) => {
    const dialogs: string[] = [];
    page.on('dialog', async dialog => {
      dialogs.push(dialog.message());
      await dialog.dismiss();
    });

    await fillMetadata(page);
    await addTemplate(page, 'hostname ${hostname}');
    await lifecycleButton(page, 'Enrich').click();
    await lifecycleButton(page, 'Publish').click();

    await expect(page).toHaveURL(new RegExp(`#/packages/package/${EXISTING_ID}$`));
    expect(dialogs).toEqual([]);
  });
});

test.describe('Leaving the package creation page', () => {
  test('does not ask when nothing was entered', async ({ page }) => {
    const dialogs: string[] = [];
    page.on('dialog', async dialog => {
      dialogs.push(dialog.message());
      await dialog.accept();
    });
    await page.goto('/#/packages/createPackage');
    await page.waitForLoadState('networkidle');

    await page.getByRole('link', { name: 'CBA Packages' }).click();

    await expect(page).toHaveURL(/#\/packages$/);
    expect(dialogs).toEqual([]);
  });

  test('asks when only a name was entered', async ({ page }) => {
    const dialogs: string[] = [];
    page.on('dialog', async dialog => {
      dialogs.push(dialog.message());
      await dialog.dismiss();
    });
    await page.goto('/#/packages/createPackage');
    await page.waitForLoadState('networkidle');
    await page.getByPlaceholder('Package name').fill('e2e-package');

    await page.getByRole('link', { name: 'CBA Packages' }).click();

    expect(dialogs).toEqual(['You have unsaved changes! If you leave, your changes will be lost.']);
    await expect(page).toHaveURL(/#\/packages\/createPackage$/);
  });
});
