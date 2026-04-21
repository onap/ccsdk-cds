import { test, expect, Page } from '@playwright/test';

async function mockApis(page: Page) {
    await page.route('/controllerblueprint/**', route =>
        route.fulfill({ status: 200, contentType: 'application/json', body: '{}' }));
    // searchByNames returns empty array — no variables found in DD
    await page.route('/resourcedictionary/search/by-names', route =>
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));
    await page.route('/resourcedictionary/**', route =>
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));
}

async function goToTemplateMappingTab(page: Page) {
    await page.goto('/#/packages/createPackage');
    await page.waitForSelector('.lifecycle-bar', { timeout: 15000 });
    // Open TEMPLATE & MAPPING tab
    await page.locator('#nav-template-tab').click();
    await page.waitForSelector('app-templ-mapp-creation', { timeout: 5000 });
}

async function openMappingAccordion(page: Page) {
    const isOpen = await page.locator('#collapseTwo.show').count();
    if (!isOpen) {
        await page.locator('#mappingTab').click();
        await page.waitForSelector('#collapseTwo.show', { timeout: 3000 });
    }
}

test.describe('Unmapped variable indicator', () => {
    test.beforeEach(async ({ page }) => {
        await mockApis(page);
    });

    test('no warning shown when template editor is empty', async ({ page }) => {
        await goToTemplateMappingTab(page);
        await expect(page.locator('.unmapped-vars-warning')).not.toBeVisible();
    });

    test('warning appears when template has variables not in mapping', async ({ page }) => {
        await goToTemplateMappingTab(page);
        await openMappingAccordion(page);

        // Inject template content and trigger updateUnmappedVars via Angular component
        await page.evaluate(() => {
            const el = document.querySelector('app-templ-mapp-creation');
            if (!el) { return; }
            // Angular 8 development mode exposes ng.probe
            const probe = (window as any).ng && (window as any).ng.probe
                ? (window as any).ng.probe(el)
                : null;
            if (probe && probe.componentInstance) {
                const comp = probe.componentInstance;
                comp.templateFileContent = '$stream-count $undefined-var $another-missing';
                comp.templateExt = 'vtl';
                comp.mappingRes = [];
                comp.updateUnmappedVars();
                comp.cdr.detectChanges();
            }
        });

        await expect(page.locator('.unmapped-vars-warning')).toBeVisible({ timeout: 3000 });
        await expect(page.locator('.unmapped-vars-warning')).toContainText('template variable');
    });

    test('warning lists each unmapped variable by name', async ({ page }) => {
        await goToTemplateMappingTab(page);

        await page.evaluate(() => {
            const el = document.querySelector('app-templ-mapp-creation');
            if (!el) { return; }
            const probe = (window as any).ng && (window as any).ng.probe
                ? (window as any).ng.probe(el)
                : null;
            if (probe && probe.componentInstance) {
                const comp = probe.componentInstance;
                comp.templateFileContent = '$foo $bar';
                comp.templateExt = 'vtl';
                comp.mappingRes = [];
                comp.updateUnmappedVars();
                comp.cdr.detectChanges();
            }
        });

        await expect(page.locator('.unmapped-vars-warning code')).toContainText(['$foo', '$bar']);
    });

    test('success banner shown when all variables are mapped', async ({ page }) => {
        await goToTemplateMappingTab(page);
        await openMappingAccordion(page);

        await page.evaluate(() => {
            const el = document.querySelector('app-templ-mapp-creation');
            if (!el) { return; }
            const probe = (window as any).ng && (window as any).ng.probe
                ? (window as any).ng.probe(el)
                : null;
            if (probe && probe.componentInstance) {
                const comp = probe.componentInstance;
                comp.templateFileContent = '$foo';
                comp.templateExt = 'vtl';
                comp.mappingRes = [{ name: 'foo' }];
                comp.updateUnmappedVars();
                comp.cdr.detectChanges();
            }
        });

        await expect(page.locator('.alert-success')).toBeVisible({ timeout: 3000 });
        await expect(page.locator('.alert-success')).toContainText('All template variables are mapped');
    });

    test('warning disappears after all variables are mapped', async ({ page }) => {
        await goToTemplateMappingTab(page);
        await openMappingAccordion(page);

        // Start with an unmapped variable
        await page.evaluate(() => {
            const el = document.querySelector('app-templ-mapp-creation');
            if (!el) { return; }
            const probe = (window as any).ng && (window as any).ng.probe
                ? (window as any).ng.probe(el)
                : null;
            if (!probe || !probe.componentInstance) { return; }
            const comp = probe.componentInstance;
            comp.templateFileContent = '$foo';
            comp.templateExt = 'vtl';
            comp.mappingRes = [];
            comp.updateUnmappedVars();
            comp.cdr.detectChanges();
        });

        await expect(page.locator('.unmapped-vars-warning')).toBeVisible({ timeout: 3000 });

        // Now add the mapping
        await page.evaluate(() => {
            const el = document.querySelector('app-templ-mapp-creation');
            if (!el) { return; }
            const probe = (window as any).ng && (window as any).ng.probe
                ? (window as any).ng.probe(el)
                : null;
            if (!probe || !probe.componentInstance) { return; }
            const comp = probe.componentInstance;
            comp.mappingRes = [{ name: 'foo' }];
            comp.updateUnmappedVars();
            comp.cdr.detectChanges();
        });

        await expect(page.locator('.unmapped-vars-warning')).not.toBeVisible({ timeout: 3000 });
        await expect(page.locator('.alert-success')).toBeVisible();
    });

    test('Use Current Template Instance triggers unmapped vars check', async ({ page }) => {
        // Mock the DD search to return no results (all vars unmapped)
        await page.route('/resourcedictionary/search/by-names', route =>
            route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));

        await goToTemplateMappingTab(page);

        // Set template content and open the mapping accordion via evaluate
        await page.evaluate(() => {
            const el = document.querySelector('app-templ-mapp-creation');
            if (!el) { return; }
            const probe = (window as any).ng && (window as any).ng.probe
                ? (window as any).ng.probe(el)
                : null;
            if (!probe || !probe.componentInstance) { return; }
            const comp = probe.componentInstance;
            comp.templateFileContent = '$orphan_var';
            comp.templateExt = 'vtl';
            comp.cdr.detectChanges();
        });

        // Open the Manage Mapping accordion
        await openMappingAccordion(page);

        // Click "Use Current Template Instance"
        await page.locator('text=Use Current Template Instance').click();

        // Wait for the API response (mocked empty)
        await page.waitForTimeout(500);

        await expect(page.locator('.unmapped-vars-warning')).toBeVisible({ timeout: 3000 });
    });
});
