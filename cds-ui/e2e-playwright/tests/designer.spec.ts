/*
 * designer.spec.ts
 *
 * End-to-end tests for the Designer canvas, focusing on the canvas panning
 * feature.  These tests verify:
 *   1. Page structure  – the designer component and JointJS board paper render.
 *   2. Cursor feedback – the canvas shows a "grab" cursor and switches to
 *      "grabbing" while the user is panning.
 *   3. Canvas panning  – dragging on blank canvas space translates the JointJS
 *      viewport (the g.joint-layers element's transform attribute changes).
 *
 * Fixture data (mock-processor/fixtures/blueprints.json):
 *   RT-resource-resolution  id: 66bfe8a0-4789-4b5d-ad7f-f2157e3a2021  v1.0.0
 *
 * The mock-processor serves:
 *   GET /api/v1/blueprint-model/:id            → single blueprint metadata
 *   GET /api/v1/blueprint-model/download/…     → RT-resource-resolution-1.0.0.zip
 *
 * Panning tests click/drag in the bottom-right 20 % of the visible canvas
 * area.  Action nodes rendered by dagre are placed from the top-left, so the
 * bottom-right corner is reliably free of cells (blank canvas), ensuring
 * blank:pointerdown fires and drives the pan logic.
 */

import { test, expect } from '@playwright/test';

// ── fixture constants ─────────────────────────────────────────────────────────

// RT-resource-resolution 1.0.0 – first entry in blueprints.json
const FIXTURE_ID = '66bfe8a0-4789-4b5d-ad7f-f2157e3a2021';
const DESIGNER_URL = `/#/packages/designer/${FIXTURE_ID};actionName=`;

// ── helpers ───────────────────────────────────────────────────────────────────

async function gotoDesigner(page: import('@playwright/test').Page) {
    await page.goto(DESIGNER_URL);
    await page.waitForLoadState('networkidle');
}

/** Wait until the JointJS paper SVG and its layers group are in the DOM. */
async function waitForBoardPaper(page: import('@playwright/test').Page) {
    await expect(page.locator('#board-paper svg')).toBeAttached({ timeout: 30_000 });
    await expect(page.locator('#board-paper .joint-layers')).toBeAttached({ timeout: 15_000 });
}

/**
 * Parse tx/ty from a JointJS transform string.
 * JointJS uses "matrix(sx,shy,shx,sy,tx,ty)" notation.
 */
function parseTranslate(transform: string): { tx: number; ty: number } {
    const m = transform.match(
        /matrix\([\d.e+-]+,[\d.e+-]+,[\d.e+-]+,[\d.e+-]+,([-\d.e+-]+),([-\d.e+-]+)\)/,
    );
    if (m) { return { tx: parseFloat(m[1]), ty: parseFloat(m[2]) }; }

    const t = transform.match(/translate\(([-\d.e+-]+)[, ]*([-\d.e+-]+)\)/);
    if (t) { return { tx: parseFloat(t[1]), ty: parseFloat(t[2]) }; }

    return { tx: 0, ty: 0 };
}

/** Read the current translate of the JointJS board paper viewport from the DOM. */
async function getBoardTranslate(
    page: import('@playwright/test').Page,
): Promise<{ tx: number; ty: number }> {
    const transform =
        (await page.locator('#board-paper .joint-layers').getAttribute('transform')) ?? '';
    return parseTranslate(transform);
}

// ── page structure ────────────────────────────────────────────────────────────

test.describe('Designer – page structure', () => {
    test.beforeEach(async ({ page }) => {
        await gotoDesigner(page);
        await waitForBoardPaper(page);
    });

    test('app-designer component is rendered', async ({ page }) => {
        await expect(page.locator('app-designer')).toBeAttached({ timeout: 10_000 });
    });

    test('#board-paper element is present', async ({ page }) => {
        await expect(page.locator('#board-paper')).toBeAttached();
    });

    test('#board-paper contains a JointJS SVG', async ({ page }) => {
        await expect(page.locator('#board-paper svg')).toBeAttached();
    });

    test('#board-paper SVG has JointJS layers group', async ({ page }) => {
        await expect(page.locator('#board-paper .joint-layers')).toBeAttached();
    });
});
// ── Designer/Scripting button overflow ───────────────────────────────────────
//
// At the Bootstrap lg breakpoint (992 px) the navbar-collapse content can fill
// the whole row, leaving no room for the Designer/Scripting btn-group if it is
// still inside .navbar-collapse.  The fix moves the btn-group to be a direct
// flex child of the <nav> with ml-auto, so it stays pinned at the right edge
// regardless of how full the collapsible area is.

test.describe('Designer – Designer/Scripting buttons stay within edit navbar', () => {
    // Run at exactly the lg breakpoint – the tightest valid width.
    test.use({ viewport: { width: 992, height: 768 } });

    test.beforeEach(async ({ page }) => {
        await gotoDesigner(page);
        await waitForBoardPaper(page);
    });

    /**
     * Returns true when the button's bounding box is fully contained within
     * the nav's bounding box (both vertically and horizontally).
     */
    async function isInsideNav(
        page: import('@playwright/test').Page,
        btnSelector: string,
    ): Promise<boolean> {
        return page.evaluate((sel: string) => {
            const nav  = document.querySelector<HTMLElement>('nav.editNavbar');
            const btn  = document.querySelector<HTMLElement>(sel);
            if (!nav || !btn) { return false; }
            const nb = nav.getBoundingClientRect();
            const bb = btn.getBoundingClientRect();
            return (
                bb.top    >= nb.top    - 1 &&
                bb.bottom <= nb.bottom + 1 &&
                bb.left   >= nb.left   - 1 &&
                bb.right  <= nb.right  + 1
            );
        }, btnSelector);
    }

    test('Designer button is contained within the edit navbar at 992 px viewport', async ({ page }) => {
        expect(await isInsideNav(page, '.editNavbar .btn.topologySource')).toBe(true);
    });

    test('Scripting button is contained within the edit navbar at 992 px viewport', async ({ page }) => {
        expect(await isInsideNav(page, '.editNavbar .btn.topologyView')).toBe(true);
    });

    test('Designer button is visible (not clipped off-screen) at 992 px viewport', async ({ page }) => {
        await expect(page.locator('.editNavbar .btn.topologySource')).toBeInViewport();
    });

    test('Scripting button is visible (not clipped off-screen) at 992 px viewport', async ({ page }) => {
        await expect(page.locator('.editNavbar .btn.topologyView')).toBeInViewport();
    });
});
// ── cursor feedback ───────────────────────────────────────────────────────────

test.describe('Designer – canvas cursor', () => {
    test.beforeEach(async ({ page }) => {
        await gotoDesigner(page);
        await waitForBoardPaper(page);
    });

    test('board paper SVG has grab cursor by default', async ({ page }) => {
        const cursor = await page
            .locator('#board-paper svg')
            .evaluate(el => window.getComputedStyle(el).cursor);
        expect(cursor).toBe('grab');
    });

    test('cursor changes to grabbing while mouse button is held on blank canvas', async ({ page }) => {
        const boardBox = await page.locator('#board-paper').boundingBox();
        if (!boardBox) { throw new Error('#board-paper has no bounding box'); }

        // Bottom-right 20 % of the canvas is blank (no action nodes rendered there)
        const startX = boardBox.x + boardBox.width * 0.8;
        const startY = boardBox.y + boardBox.height * 0.8;

        await page.mouse.move(startX, startY);
        await page.mouse.down();

        // Read cursor while button is still held
        const cursor = await page
            .locator('#board-paper svg')
            .evaluate(el => window.getComputedStyle(el).cursor);

        await page.mouse.up();

        expect(cursor).toBe('grabbing');
    });

    test('cursor returns to grab after the mouse button is released', async ({ page }) => {
        const boardBox = await page.locator('#board-paper').boundingBox();
        if (!boardBox) { throw new Error('#board-paper has no bounding box'); }

        const startX = boardBox.x + boardBox.width * 0.8;
        const startY = boardBox.y + boardBox.height * 0.8;

        await page.mouse.move(startX, startY);
        await page.mouse.down();
        await page.mouse.move(startX + 50, startY + 50, { steps: 5 });
        await page.mouse.up();

        const cursor = await page
            .locator('#board-paper svg')
            .evaluate(el => window.getComputedStyle(el).cursor);
        expect(cursor).toBe('grab');
    });
});

// ── canvas panning ────────────────────────────────────────────────────────────

test.describe('Designer – canvas panning', () => {
    test.beforeEach(async ({ page }) => {
        await gotoDesigner(page);
        await waitForBoardPaper(page);
    });

    test('dragging on blank canvas translates the JointJS viewport', async ({ page }) => {
        const boardBox = await page.locator('#board-paper').boundingBox();
        if (!boardBox) { throw new Error('#board-paper has no bounding box'); }

        const startX = boardBox.x + boardBox.width * 0.8;
        const startY = boardBox.y + boardBox.height * 0.8;
        const dragDx = 100;
        const dragDy = 60;

        const before = await getBoardTranslate(page);

        await page.mouse.move(startX, startY);
        await page.mouse.down();
        await page.mouse.move(startX + dragDx, startY + dragDy, { steps: 10 });
        await page.mouse.up();

        const after = await getBoardTranslate(page);

        // The viewport translation must increase by the drag distance (±1 px rounding)
        expect(after.tx).toBeCloseTo(before.tx + dragDx, 0);
        expect(after.ty).toBeCloseTo(before.ty + dragDy, 0);
    });

    test('successive drags accumulate correctly in the viewport translation', async ({ page }) => {
        const boardBox = await page.locator('#board-paper').boundingBox();
        if (!boardBox) { throw new Error('#board-paper has no bounding box'); }

        const cx = boardBox.x + boardBox.width * 0.8;
        const cy = boardBox.y + boardBox.height * 0.8;

        const initial = await getBoardTranslate(page);

        // First drag: 80 px to the right
        await page.mouse.move(cx, cy);
        await page.mouse.down();
        await page.mouse.move(cx + 80, cy, { steps: 8 });
        await page.mouse.up();

        // Second drag: 50 px downward
        await page.mouse.move(cx, cy);
        await page.mouse.down();
        await page.mouse.move(cx, cy + 50, { steps: 5 });
        await page.mouse.up();

        const final = await getBoardTranslate(page);

        expect(final.tx).toBeCloseTo(initial.tx + 80, 0);
        expect(final.ty).toBeCloseTo(initial.ty + 50, 0);
    });

    test('moving the mouse without pressing does not pan the canvas', async ({ page }) => {
        const boardBox = await page.locator('#board-paper').boundingBox();
        if (!boardBox) { throw new Error('#board-paper has no bounding box'); }

        const before = await getBoardTranslate(page);

        // Freely move across the canvas without holding a button
        await page.mouse.move(
            boardBox.x + boardBox.width * 0.5,
            boardBox.y + boardBox.height * 0.5,
        );
        await page.mouse.move(
            boardBox.x + boardBox.width * 0.8,
            boardBox.y + boardBox.height * 0.8,
            { steps: 10 },
        );

        const after = await getBoardTranslate(page);

        expect(after.tx).toBeCloseTo(before.tx, 1);
        expect(after.ty).toBeCloseTo(before.ty, 1);
    });

    test('releasing the mouse outside the canvas stops panning', async ({ page }) => {
        const boardBox = await page.locator('#board-paper').boundingBox();
        if (!boardBox) { throw new Error('#board-paper has no bounding box'); }

        const startX = boardBox.x + boardBox.width * 0.8;
        const startY = boardBox.y + boardBox.height * 0.8;

        // Drag inside canvas, then move outside and release
        await page.mouse.move(startX, startY);
        await page.mouse.down();
        await page.mouse.move(startX + 50, startY + 30, { steps: 5 });
        await page.mouse.move(5, 5, { steps: 5 }); // outside viewport
        await page.mouse.up();

        const afterFirstDrag = await getBoardTranslate(page);

        // Move back inside canvas without pressing – must NOT pan
        await page.mouse.move(startX, startY, { steps: 5 });
        await page.mouse.move(startX + 100, startY + 100, { steps: 10 });

        const afterFreeMouse = await getBoardTranslate(page);

        expect(afterFreeMouse.tx).toBeCloseTo(afterFirstDrag.tx, 1);
        expect(afterFreeMouse.ty).toBeCloseTo(afterFirstDrag.ty, 1);
    });
});

// ── canvas element click opens sidepane ───────────────────────────────────────
//
// RT-resource-resolution topology:
//   action:   resource-resolution
//   function: helloworld  (embedded board.FunctionElement inside the action)
//
// The sidepane opens only on a clean click (no drag). These tests verify:
//   1. A clean click on an action node opens the Action Attributes sidepane.
//   2. A drag on an action node does NOT open the sidepane.
//   3. A clean click on a function node opens the Function Attributes sidepane.

test.describe('Designer – canvas element click opens sidepane', () => {

    /** Returns the viewport center {x, y} of the first JointJS canvas element
     *  whose #label tspan contains exactly the given text. */
    async function getCanvasElementCenter(
        page: import('@playwright/test').Page,
        label: string,
    ): Promise<{ x: number; y: number }> {
        // Wait until the element is rendered on the board paper.
        await expect(
            page.locator('#board-paper tspan').filter({ hasText: label }).first(),
        ).toBeAttached({ timeout: 30_000 });

        const center = await page.evaluate((lbl: string) => {
            const tspans = Array.from(
                document.querySelectorAll<SVGTSpanElement>('#board-paper tspan[id="label"]'),
            );
            const match = tspans.find(t => t.textContent?.trim() === lbl);
            if (!match) { return null; }
            const r = match.getBoundingClientRect();
            return { x: r.left + r.width / 2, y: r.top + r.height / 2 };
        }, label);

        if (!center) { throw new Error(`Canvas element labeled "${label}" not found`); }
        return center;
    }

    test.beforeEach(async ({ page }) => {
        await gotoDesigner(page);
        await waitForBoardPaper(page);
    });

    test('clean click on an action node opens the Action Attributes sidepane', async ({ page }) => {
        const { x, y } = await getCanvasElementCenter(page, 'resource-resolution');

        await page.mouse.move(x, y);
        await page.mouse.down();
        await page.mouse.up();

        await expect(
            page.locator('h6:has-text("Action Attributes")'),
        ).toBeInViewport({ timeout: 5_000 });
    });

    test('dragging an action node does not open the sidepane', async ({ page }) => {
        const { x, y } = await getCanvasElementCenter(page, 'resource-resolution');

        await page.mouse.move(x, y);
        await page.mouse.down();
        // Move well beyond the 5 px drag threshold
        await page.mouse.move(x + 40, y + 20, { steps: 4 });
        await page.mouse.up();

        await expect(
            page.locator('h6:has-text("Action Attributes")'),
        ).not.toBeInViewport({ timeout: 1_000 });
    });

    test('clean click on a function node opens the Function Attributes sidepane', async ({ page }) => {
        const { x, y } = await getCanvasElementCenter(page, 'helloworld');

        await page.mouse.move(x, y);
        await page.mouse.down();
        await page.mouse.up();

        await expect(
            page.locator('h6:has-text("Function Attributes")'),
        ).toBeInViewport({ timeout: 5_000 });
    });
});

// ── Action Name Editing ───────────────────────────────────────────────────────
//
// These tests verify that the Action Name input in the Action Attributes
// side-pane is editable and that renaming an action is reflected everywhere.

// ── constants ──────────────────────────────────────────────────────────────────

/** Use the first fixture blueprint so the mock-processor can answer the
 *  metadata request even if Playwright's route intercept fires too late. */
const BLUEPRINT_ID = 'a1b2c3d4-0001-0001-0001-000000000001';

/**
 * A valid empty ZIP archive (end-of-central-directory record only, 22 bytes).
 * JSZip loads this without error and iterates zero entries, so the extraction
 * service exits cleanly without populating the topology store.  That is fine
 * because "New Action" creates actions entirely in-memory.
 */
const EMPTY_ZIP_HEX = '504b0506' + '00'.repeat(18);

// ── helpers ────────────────────────────────────────────────────────────────────

/** Navigate to the designer for a given blueprint id and wait for the
 *  Angular component to be attached to the DOM. */
async function openDesigner(page: import('@playwright/test').Page, id = BLUEPRINT_ID) {
    // Intercept the blueprint-metadata call so the designer can resolve the
    // package name/version without depending on a specific mock-processor state.
    await page.route(`**/controllerblueprint/${id}`, async route => {
        await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
                id,
                artifactName: 'vFW-CDS',
                artifactVersion: '1.0.0',
                tags: 'vFW,firewall,demo',
            }),
        });
    });

    // Intercept the package-download call and return an empty-but-valid ZIP so
    // the extraction service does not throw and the component keeps running.
    await page.route('**/controllerblueprint/download-blueprint/**', async route => {
        await route.fulfill({
            status: 200,
            contentType: 'application/zip',
            body: Buffer.from(EMPTY_ZIP_HEX, 'hex'),
        });
    });

    await page.goto(`/#/packages/designer/${id}`);

    // Wait for the Angular designer component to be mounted.
    await expect(page.locator('app-designer')).toBeAttached({ timeout: 20_000 });
}

/** Click the "New Action" button and return the first action label that appears
 *  in the left-hand sidebar list. */
async function createNewAction(page: import('@playwright/test').Page) {
    await page.locator('button.new-action').click();

    // The action label appears inside .actionsList once Angular renders the
    // *ngFor list.
    const actionLabel = page.locator('.actionsList label').first();
    await expect(actionLabel).toBeVisible({ timeout: 10_000 });
    return actionLabel;
}

/** Open the Action Attributes pane for the given label element and return the
 *  action-name <input> inside it. */
async function openActionAttributes(page: import('@playwright/test').Page, actionLabel: import('@playwright/test').Locator) {
    await actionLabel.click();

    const pane = page.locator('app-action-attributes');
    await expect(pane).toBeVisible({ timeout: 10_000 });

    const nameInput = pane.locator('#actionName');
    await expect(nameInput).toBeVisible({ timeout: 5_000 });
    return nameInput;
}

// ── tests ──────────────────────────────────────────────────────────────────────

test.describe('Designer – Action Name Editing', () => {

    // ── 1. input is editable ────────────────────────────────────────────────

    test('action name input is editable (not readonly)', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        const nameInput = await openActionAttributes(page, actionLabel);

        // The `readonly` attribute must be absent after the fix.
        await expect(nameInput).not.toHaveAttribute('readonly');
        // The input must be enabled so the user can interact with it.
        await expect(nameInput).toBeEnabled();
    });

    // ── 2. initial value matches sidebar label ──────────────────────────────

    test('action name input shows the generated name (e.g. "Action1")', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        const sidebarText = (await actionLabel.textContent() ?? '').trim();

        const nameInput = await openActionAttributes(page, actionLabel);

        await expect(nameInput).toHaveValue(sidebarText);
    });

    // ── 3. renaming updates the sidebar ─────────────────────────────────────

    test('renamed action is reflected in the sidebar', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        const nameInput = await openActionAttributes(page, actionLabel);

        const newName = 'MyRenamedAction';

        // Replace the value and trigger the (change) event by pressing Tab.
        await nameInput.fill(newName);
        await nameInput.press('Tab');

        // The input itself should show the new name.
        await expect(nameInput).toHaveValue(newName);

        // The sidebar label must display the new name.
        await expect(page.locator('.actionsList label').first()).toContainText(newName);
    });

    // ── 4. clearing the field is rejected – original name preserved ─────────

    test('clearing the action name is a no-op (original name preserved)', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        const sidebarText = (await actionLabel.textContent() ?? '').trim();

        const nameInput = await openActionAttributes(page, actionLabel);

        // Clear the field and tab away – the component should reject an empty name.
        await nameInput.fill('');
        await nameInput.press('Tab');

        // The sidebar label must still show the original name.
        await expect(page.locator('.actionsList label').first()).toContainText(sidebarText);
        // The input should also be restored to the original name.
        await expect(nameInput).toHaveValue(sidebarText);
    });

    // ── 5. action attribute pane buttons are not occluded by navbar ─────────

    test('Close button in action attributes pane is not occluded by navbar-collapse', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        await openActionAttributes(page, actionLabel);

        const closeBtn = page.locator('.attributesSideBar .closeBar').first();
        await expect(closeBtn).toBeVisible({ timeout: 5_000 });

        // Verify the top quarter of the button is hit-testable (not covered by
        // the navbar-collapse overlay).
        const hit = await closeBtn.evaluate(el => {
            const r = el.getBoundingClientRect();
            // Sample a point in the upper quarter of the button
            const x = r.left + r.width / 2;
            const y = r.top + r.height * 0.25;
            const topEl = document.elementFromPoint(x, y);
            return el === topEl || el.contains(topEl);
        });
        expect(hit).toBe(true);
    });

    test('View Action Source button is not occluded by navbar-collapse', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        await openActionAttributes(page, actionLabel);

        const viewBtn = page.locator('.attributesSideBar .btn.view-source').first();
        await expect(viewBtn).toBeVisible({ timeout: 5_000 });

        const hit = await viewBtn.evaluate(el => {
            const r = el.getBoundingClientRect();
            const x = r.left + r.width / 2;
            const y = r.top + r.height * 0.25;
            const topEl = document.elementFromPoint(x, y);
            return el === topEl || el.contains(topEl);
        });
        expect(hit).toBe(true);
    });

    test('Delete Action button is not occluded by navbar-collapse', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        await openActionAttributes(page, actionLabel);

        const deleteBtn = page.locator('.attributesSideBar .btn.trash-item').first();
        await expect(deleteBtn).toBeVisible({ timeout: 5_000 });

        const hit = await deleteBtn.evaluate(el => {
            const r = el.getBoundingClientRect();
            const x = r.left + r.width / 2;
            const y = r.top + r.height * 0.25;
            const topEl = document.elementFromPoint(x, y);
            return el === topEl || el.contains(topEl);
        });
        expect(hit).toBe(true);
    });

    test('clicking Close button actually closes the action attributes pane', async ({ page }) => {
        await openDesigner(page);

        const actionLabel = await createNewAction(page);
        await openActionAttributes(page, actionLabel);

        const closeBtn = page.locator('.attributesSideBar .closeBar').first();
        await expect(closeBtn).toBeVisible({ timeout: 5_000 });

        // Click in the upper portion of the button (where the overlay was blocking)
        const box = await closeBtn.boundingBox();
        if (!box) { throw new Error('.closeBar has no bounding box'); }
        await page.mouse.click(box.x + box.width / 2, box.y + box.height * 0.25);

        // The pane should close — the title bar should no longer be in the viewport.
        await expect(
            page.locator('h6:has-text("Action Attributes")'),
        ).not.toBeInViewport({ timeout: 5_000 });
    });

    // ── 6. multiple actions can be independently renamed ────────────────────

    test('two actions can be renamed independently', async ({ page }) => {
        await openDesigner(page);

        // Create first action and rename it.
        const firstLabel = await createNewAction(page);
        const firstInput = await openActionAttributes(page, firstLabel);
        await firstInput.fill('FirstAction');
        await firstInput.press('Tab');
        await expect(page.locator('.actionsList label').first()).toContainText('FirstAction');

        // Close the pane before creating the second action.
        // Use .first() because both sidebars (action + function) each have a .closeBar in the DOM.
        await page.locator('.closeBar').first().click();

        // Create a second action.
        await page.locator('button.new-action').click();
        const labels = page.locator('.actionsList label');
        await expect(labels).toHaveCount(2, { timeout: 10_000 });

        // Open the second action and rename it.
        const secondInput = await openActionAttributes(page, labels.nth(1));
        await secondInput.fill('SecondAction');
        await secondInput.press('Tab');

        // Both labels should appear in the sidebar.
        await expect(page.locator('.actionsList label').nth(0)).toContainText('FirstAction');
        await expect(page.locator('.actionsList label').nth(1)).toContainText('SecondAction');
    });

    test('after deleting Action1, the next new action is Action1 again', async ({ page }) => {
        await openDesigner(page);

        const firstActionLabel = await createNewAction(page);
        await expect(firstActionLabel).toContainText('Action1');
        await openActionAttributes(page, firstActionLabel);

        const deleteBtn = page.locator('.attributesSideBar .btn.trash-item').first();
        await expect(deleteBtn).toBeVisible({ timeout: 5_000 });
        await deleteBtn.click();
        await expect(page.locator('#deleteActionModal')).toBeVisible({ timeout: 5_000 });

        await page.locator('#deleteActionModal .btn-danger').click();
        await expect(page.locator('#deleteActionModal')).not.toBeVisible({ timeout: 5_000 });
        await expect(page.locator('.actionsList label')).toHaveCount(0, { timeout: 5_000 });

        const secondActionLabel = await createNewAction(page);
        await expect(secondActionLabel).toContainText('Action1');
    });
});

// ── Action Deletion ──────────────────────────────────────────────────────────
//
// These tests verify that clicking the Delete (trash) button in the Action
// Attributes sidebar, confirming the modal, actually removes the action from
// the sidebar list and the canvas, closes the modal AND both attribute panes,
// and — critically — does NOT re-open either sidebar afterwards (a race
// condition between Bootstrap modal dismiss and Angular change detection).

test.describe('Designer – Action Deletion', () => {

    async function openFixtureActionAttributes(page: import('@playwright/test').Page) {
        await gotoDesigner(page);
        await waitForBoardPaper(page);

        const actionLabels = page.locator('.actionsList label');
        await expect(actionLabels.first()).toBeVisible({ timeout: 10_000 });
        const actionText = (await actionLabels.first().textContent() ?? '').trim();
        await actionLabels.first().click();

        await expect(page.locator('h6:has-text("Action Attributes")')).toBeVisible({ timeout: 5_000 });
        return actionText;
    }

    /** Open the delete-confirmation modal from the action attributes pane. */
    async function clickDeleteTrash(page: import('@playwright/test').Page) {
        const trashBtn = page.locator('.attributesSideBar .btn.trash-item').first();
        await expect(trashBtn).toBeVisible({ timeout: 5_000 });
        await trashBtn.click();

        // Wait for the Bootstrap modal to be fully visible.
        const modal = page.locator('#deleteActionModal');
        await expect(modal).toBeVisible({ timeout: 5_000 });
        return modal;
    }

    async function dragBlankCanvas(
        page: import('@playwright/test').Page,
        dx = 90,
        dy = 60,
    ): Promise<{ before: { tx: number; ty: number }; after: { tx: number; ty: number } }> {
        const boardBox = await page.locator('#board-paper').boundingBox();
        if (!boardBox) { throw new Error('#board-paper has no bounding box'); }

        const startX = boardBox.x + boardBox.width * 0.8;
        const startY = boardBox.y + boardBox.height * 0.8;
        const before = await getBoardTranslate(page);

        await page.mouse.move(startX, startY);
        await page.mouse.down();
        await page.mouse.move(startX + dx, startY + dy, { steps: 10 });
        await page.mouse.up();

        const after = await getBoardTranslate(page);
        return { before, after };
    }

    test('deleting an action removes it from the sidebar list', async ({ page }) => {
        const actionText = await openFixtureActionAttributes(page);

        await clickDeleteTrash(page);
        await page.locator('#deleteActionModal .btn-danger').click();

        // The action must no longer appear in the sidebar list.
        await expect(
            page.locator('.actionsList label', { hasText: actionText }),
        ).toHaveCount(0, { timeout: 5_000 });
    });

    test('deleting an action closes both attribute panes', async ({ page }) => {
        await openFixtureActionAttributes(page);

        await clickDeleteTrash(page);
        await page.locator('#deleteActionModal .btn-danger').click();

        // Wait for the modal to be fully dismissed (Bootstrap animation).
        await expect(page.locator('#deleteActionModal')).not.toBeVisible({ timeout: 5_000 });

        await expect(
            page.locator('h6:has-text("Action Attributes")'),
        ).not.toBeInViewport({ timeout: 5_000 });
        await expect(
            page.locator('h6:has-text("Function Attributes")'),
        ).not.toBeInViewport({ timeout: 5_000 });

        // Regression guard for the browser behavior: left actions pane must remain visible.
        await expect(page.locator('.controllerSidebar .actionsList')).toBeInViewport({ timeout: 5_000 });
    });

    test('deleting an action dismisses the confirmation modal', async ({ page }) => {
        await openFixtureActionAttributes(page);

        await clickDeleteTrash(page);
        await page.locator('#deleteActionModal .btn-danger').click();

        await expect(page.locator('#deleteActionModal')).not.toBeVisible({ timeout: 5_000 });
    });

    test('canvas can still be dragged after deleting an action', async ({ page }) => {
        await openFixtureActionAttributes(page);

        await clickDeleteTrash(page);
        await page.locator('#deleteActionModal .btn-danger').click();

        await expect(page.locator('#deleteActionModal')).not.toBeVisible({ timeout: 5_000 });
        await expect(page.locator('.modal-backdrop')).toHaveCount(0, { timeout: 5_000 });

        const { before, after } = await dragBlankCanvas(page);
        expect(after.tx).toBeCloseTo(before.tx + 90, 0);
        expect(after.ty).toBeCloseTo(before.ty + 60, 0);
    });

    test('no attribute sidebar re-opens and left actions pane stays visible for 1 second after delete', async ({ page }) => {
        await openFixtureActionAttributes(page);

        await clickDeleteTrash(page);
        await page.locator('#deleteActionModal .btn-danger').click();

        // Wait for the modal to be fully dismissed (Bootstrap animation).
        await expect(page.locator('#deleteActionModal')).not.toBeVisible({ timeout: 5_000 });

        // Poll every 100 ms for 1 second to assert neither sidebar re-opens.
        for (let elapsed = 0; elapsed < 1000; elapsed += 100) {
            await page.waitForTimeout(100);

            const actionPaneOnScreen = await page.locator(
                'h6:has-text("Action Attributes")',
            ).isVisible().catch(() => false);
            const functionPaneOnScreen = await page.locator(
                'h6:has-text("Function Attributes")',
            ).isVisible().catch(() => false);
            const actionsListVisible = await page.locator(
                '.controllerSidebar .actionsList',
            ).isVisible().catch(() => false);

            expect(actionPaneOnScreen,
                `Action pane unexpectedly visible ${elapsed + 100} ms after delete`,
            ).toBe(false);
            expect(functionPaneOnScreen,
                `Function pane unexpectedly visible ${elapsed + 100} ms after delete`,
            ).toBe(false);
            expect(actionsListVisible,
                `Left actions list unexpectedly hidden ${elapsed + 100} ms after delete`,
            ).toBe(true);
        }
    });

    test('cancelling the delete modal does NOT remove the action', async ({ page }) => {
        const actionText = await openFixtureActionAttributes(page);

        await clickDeleteTrash(page);
        // Click Cancel instead of Delete.
        await page.locator('#deleteActionModal .btn-secondary').click();

        await expect(page.locator('#deleteActionModal')).not.toBeVisible({ timeout: 5_000 });

        // The action must still be in the sidebar list.
        await expect(
            page.locator('.actionsList label', { hasText: actionText }),
        ).toHaveCount(1);
    });
});
