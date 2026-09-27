import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 120_000,
  use: {
    baseURL: 'http://localhost:4200',
    headless: true,
    video: 'on',
    trace: 'retain-on-failure',
    launchOptions: {
      slowMo: 1_200,
    },
  },
  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    reuseExistingServer: true,
  },
});
