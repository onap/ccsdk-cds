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
            page.locator('#board-paper tspan').filter({ hasText: label }),
        ).toBeAttached({ timeout: 15_000 });

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
