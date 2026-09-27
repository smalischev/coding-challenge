import { Browser, expect, Page, test } from '@playwright/test';

test('developer sees an error when joining a non-existent session', async ({ page }) => {
  const username = `e2e-invalid-session-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const password = 'a-secure-test-password';

  await registerAndLogin(page, username, password, 'DEVELOPER');
  await page.getByLabel('Session-ID').fill('00000000-0000-0000-0000-000000000000');
  await pause(page);
  await page.getByRole('button', { name: 'Beitreten' }).click();

  await expect(page.getByText('Der Beitritt zur Sitzung ist fehlgeschlagen. Prüfe die Session-ID.')).toBeVisible();
  await pause(page);
});

test('developer cannot join a session after it has been released', async ({ browser, page }) => {
  const suffix = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const scrumMasterName = `e2e-released-session-owner-${suffix}`;
  const developerName = `e2e-late-developer-${suffix}`;
  const password = 'a-secure-test-password';

  await registerAndLogin(page, scrumMasterName, password, 'SCRUM_MASTER');
  await page.getByLabel('GitLab-Projekt-ID').fill('1');
  await page.getByLabel('Issue-IID').fill('1');
  await pause(page);
  await page.getByRole('button', { name: 'Sitzung erstellen' }).click();
  const sessionIdElement = page.locator('header .session strong');
  await expect(sessionIdElement).not.toHaveText('', { timeout: 10_000 });
  await pause(page);
  const sessionId = await sessionIdElement.textContent();

  if (!sessionId)
    throw new Error('The created session ID was not displayed.');

  await page.getByRole('button', { name: 'Für Schätzung freigeben' }).click();
  await pause(page);

  const developerContext = await browser.newContext({
    recordVideo: {
      dir: 'test-results/developer-videos',
      size: { width: 1280, height: 720 },
    },
  });
  const developerPage = await developerContext.newPage();
  await registerAndLogin(developerPage, developerName, password, 'DEVELOPER');
  await developerPage.getByLabel('Session-ID').fill(sessionId);
  await pause(developerPage);
  await developerPage.getByRole('button', { name: 'Beitreten' }).click();

  await expect(developerPage.getByText('Der Beitritt zur Sitzung ist fehlgeschlagen. Prüfe die Session-ID.')).toBeVisible();
  await pause(developerPage);
  await developerContext.close();
});

async function registerAndLogin(page: Page, username: string, password: string, role: 'DEVELOPER' | 'SCRUM_MASTER'): Promise<void> {
  await page.goto('/register');
  await pause(page);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByLabel('Role').selectOption(role);
  await pause(page);
  await page.getByRole('button', { name: 'Create account' }).click();
  await expect(page).toHaveURL(/\/login$/);
  await pause(page);

  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await pause(page);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/\/poker$/);
  await pause(page);
}

function pause(page: Page): Promise<void> {
  return page.waitForTimeout(2_000);
}
