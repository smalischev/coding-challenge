import { Browser, expect, Page, test } from '@playwright/test';

test('users complete the planning poker happy path through the UI', async ({ browser, page }) => {
  const suffix = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const scrumMaster = createUser(`e2e-scrum-master-${suffix}`, 'SCRUM_MASTER');
  const developerOne = createUser(`e2e-developer-one-${suffix}`, 'DEVELOPER');
  const developerTwo = createUser(`e2e-developer-two-${suffix}`, 'DEVELOPER');

  await registerAndLogin(page, scrumMaster);
  await page.getByLabel('GitLab-Projekt-ID').fill('1');
  await page.getByLabel('Issue-IID').fill('1');
  await pause(page);
  await page.getByRole('button', { name: 'Sitzung erstellen' }).click();
  const sessionIdElement = page.locator('header .session strong');
  await expect(sessionIdElement).not.toHaveText('', { timeout: 10_000 });
  await pause(page);
  const sessionId = await sessionIdElement.textContent();
  if (!sessionId) throw new Error('The created session ID was not displayed.');

  const developerOnePage = await createDeveloperPage(browser, developerOne, sessionId);
  const developerTwoPage = await createDeveloperPage(browser, developerTwo, sessionId);
  await pause(page);
  await page.getByRole('button', { name: 'Für Schätzung freigeben' }).click();
  await pause(page);

  await expect(developerOnePage.getByRole('heading', { name: 'Deine Schätzung' })).toBeVisible({ timeout: 10_000 });
  await expect(developerTwoPage.getByRole('heading', { name: 'Deine Schätzung' })).toBeVisible({ timeout: 10_000 });
  await developerOnePage.getByRole('button', { name: '5', exact: true }).click();
  await pause(page);
  await pause(page);
  await developerTwoPage.getByRole('button', { name: '8', exact: true }).click();
  await pause(page);
  await pause(page);

  await expect(page.getByText('Alle Entwickler haben geschätzt. Die Runde kann aufgedeckt werden.')).toBeVisible({ timeout: 10_000 });
  await pause(page);
  await page.getByRole('button', { name: 'Aufdecken' }).click();
  await expect(page.getByRole('heading', { name: 'Aufgedeckte Karten' })).toBeVisible();
  await pause(page);
  await page.getByRole('button', { name: 'Nach GitLab übernehmen' }).click();
  await expect(page.getByText(/Ergebnis .* wurde an GitLab übergeben/)).toBeVisible();
  await pause(page);

  await developerOnePage.context().close();
  await developerTwoPage.context().close();
});

interface User {
  username: string;
  password: string;
  role: 'SCRUM_MASTER' | 'DEVELOPER';
}

function createUser(username: string, role: User['role']): User {
  return { username, role, password: 'a-secure-test-password' };
}

async function registerAndLogin(page: Page, user: User): Promise<void> {
  await page.goto('/register');
  await pause(page);
  await page.getByLabel('Username').fill(user.username);
  await page.getByLabel('Password').fill(user.password);
  await page.getByLabel('Role').selectOption(user.role);
  await pause(page);
  await page.getByRole('button', { name: 'Create account' }).click();
  await expect(page).toHaveURL(/\/login$/);
  await pause(page);
  await page.getByLabel('Username').fill(user.username);
  await page.getByLabel('Password').fill(user.password);
  await pause(page);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/\/poker$/);
  await pause(page);
}

async function createDeveloperPage(browser: Browser, user: User, sessionId: string): Promise<Page> {
  const context = await browser.newContext({
    recordVideo: {
      dir: 'test-results/developer-videos',
      size: { width: 1280, height: 720 },
    },
  });
  const page = await context.newPage();
  await registerAndLogin(page, user);
  await page.getByLabel('Session-ID').fill(sessionId);
  await page.getByRole('button', { name: 'Beitreten' }).click();
  await expect(page.getByRole('heading', { name: 'Das Issue wurde noch nicht freigegeben.' })).toBeVisible();
  return page;
}

function pause(page: Page): Promise<void> {
  return page.waitForTimeout(2_000);
}
