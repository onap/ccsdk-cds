/*
 * console-errors.spec.ts
 *
 * Loads each top-level page and asserts that the browser reports no script
 * errors and that no request made by the page comes back 404.
 */

import { test, expect, Page } from '@playwright/test';

const PAGES = [
  '/#/packages',
  '/#/packages/createPackage',
  '/#/resource-dictionary',
  '/#/resource-dictionary/createDictionary',
  '/#/execute',
] as const;

function collectProblems(page: Page): string[] {
  const problems: string[] = [];
  page.on('pageerror', err => problems.push(`pageerror: ${err.message}`));
  page.on('console', msg => {
    if (msg.type() === 'error') {
      problems.push(`console.error: ${msg.text()}`);
    }
  });
  page.on('response', response => {
    if (response.status() === 404) {
      problems.push(`404: ${response.url()}`);
    }
  });
  return problems;
}

test.describe('Browser console', () => {
  for (const url of PAGES) {
    test(`${url} loads without script errors or 404s`, async ({ page }) => {
      const problems = collectProblems(page);

      await page.goto(url);
      await page.waitForLoadState('networkidle');

      expect(problems).toEqual([]);
    });
  }

  test('switching the script type in the package creation editor loads no missing ace mode', async ({ page }) => {
    const problems = collectProblems(page);

    await page.goto('/#/packages/createPackage');
    await page.locator('#nav-scripts-tab').click();
    await page.getByRole('button', { name: 'Create Script' }).click();
    const modal = page.locator('#createScriptsModal');
    for (const scriptType of ['Kotlin', 'Jython', 'Ansible']) {
      await modal.getByText(scriptType, { exact: true }).click();
    }
    await page.waitForLoadState('networkidle');

    expect(problems).toEqual([]);
  });
});
