import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
    testDir: './e2e/playwright',
    fullyParallel: false,
    retries: 0,
    workers: 1,
    reporter: [['list'], ['html', { open: 'never', outputFolder: 'playwright-report' }]],
    use: {
        baseURL: 'http://localhost:4200',
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'], headless: true },
        },
    ],
    webServer: {
        command: 'NODE_OPTIONS=--openssl-legacy-provider node_modules/.bin/ng serve --proxy-config proxy.conf.json --port 4200',
        url: 'http://localhost:4200',
        reuseExistingServer: true,
        timeout: 120 * 1000,
    },
});
