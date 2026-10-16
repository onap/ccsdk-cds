/*
 * page-structure.spec.ts
 *
 * End-to-end tests for the page structure shared by all dashboard pages and
 * for reaching it with the keyboard.  These tests verify:
 *   1. Landmarks  – every page has one main landmark, a labelled main
 *      navigation and exactly one level-one heading.
 *   2. Skip link  – the first Tab stop moves focus to the main content.
 *   3. Sidebar    – the current page link carries aria-current="page".
 *   4. Help menu  – the header menus open with the keyboard, report their
 *      state through aria-expanded and close on Escape.
 *   5. Welcome panel – it can be dismissed and reopened via Getting Started
 *      without a mouse.
 */

import { test, expect } from '@playwright/test';

const PAGES = ['/#/packages', '/#/packages/createPackage', '/#/resource-dictionary', '/#/execute'];

test.describe('Page structure – landmarks and headings', () => {
  for (const url of PAGES) {
    test(`${url} has one main, a main navigation and one h1`, async ({ page }) => {
      await page.goto(url);

      await expect(page.getByRole('main')).toHaveCount(1);
      await expect(page.getByRole('navigation', { name: 'Main' })).toBeAttached();
      await expect(page.getByRole('heading', { level: 1 })).toHaveCount(1);
    });
  }
});

test.describe('Page structure – keyboard entry points', () => {
  test('first Tab stop is the skip link and it focuses the main content', async ({ page }) => {
    await page.goto('/#/resource-dictionary');
    await expect(page.getByRole('main')).toBeAttached();

    await page.keyboard.press('Tab');
    const skipLink = page.getByRole('button', { name: 'Skip to main content' });
    await expect(skipLink).toBeFocused();

    await page.keyboard.press('Enter');
    await expect(page.getByRole('main')).toBeFocused();
  });

  test('sidebar marks the current page', async ({ page }) => {
    await page.goto('/#/resource-dictionary');

    const nav = page.getByRole('navigation', { name: 'Main' });
    await expect(nav.getByRole('link', { name: 'Resource Dictionary' })).toHaveAttribute('aria-current', 'page');
    await expect(nav.getByRole('link', { name: 'Packages' })).not.toHaveAttribute('aria-current', 'page');
  });

  test('help menu opens with the keyboard and closes on Escape', async ({ page }) => {
    await page.goto('/#/packages/createPackage');

    const help = page.getByRole('button', { name: 'Help' });
    await help.focus();
    await expect(help).toHaveAttribute('aria-expanded', 'false');

    await page.keyboard.press('Enter');
    await expect(help).toHaveAttribute('aria-expanded', 'true');
    await expect(page.getByRole('link', { name: /Tutorials/ })).toBeVisible();

    await page.keyboard.press('Escape');
    await expect(help).toHaveAttribute('aria-expanded', 'false');
    await expect(help).toBeFocused();
  });

  test('welcome panel can be dismissed and reopened with the keyboard', async ({ page }) => {
    await page.goto('/#/packages');

    const welcome = page.getByRole('region', { name: 'Welcome to CDS Designer UI' });
    await expect(welcome).toBeVisible();

    await page.getByRole('button', { name: 'No Thanks' }).focus();
    await page.keyboard.press('Enter');
    await expect(welcome).toBeHidden();

    await page.getByRole('button', { name: 'Help' }).focus();
    await page.keyboard.press('Enter');
    await page.getByRole('button', { name: /Getting Started/ }).focus();
    await page.keyboard.press('Enter');
    await expect(welcome).toBeVisible();
  });
});
