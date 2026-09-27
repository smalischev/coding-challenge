import { expect, Page, test } from '@playwright/test';

test('scrum master can select another issue before releasing the round', async ({ page }) => {
  await registerAndCreateSession(page);

  await page.getByLabel('Issue-IID wechseln').fill('2');
  await page.getByRole('button', { name: 'Issue auswählen' }).click();

  await expect(page.locator('.preview')).toContainText('#2 ·');
});

test('scrum master cannot change the issue after releasing the round', async ({ page }) => {
  await registerAndCreateSession(page);

  await page.getByRole('button', { name: 'Für Schätzung freigeben' }).click();

  await expect(page.getByText('Issue-IID wechseln')).not.toBeVisible();
  await expect(page.getByRole('button', { name: 'Bereits freigegeben' })).toBeVisible();
});

async function registerAndCreateSession(page: Page): Promise<void> {
  const username = `e2e-issue-owner-${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const password = 'a-secure-test-password';

  await page.goto('/register');
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByLabel('Role').selectOption('SCRUM_MASTER');
  await page.getByRole('button', { name: 'Create account' }).click();
  await expect(page).toHaveURL(/\/login$/);

  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/\/poker$/);

  await page.getByLabel('GitLab-Projekt-ID').fill('1');
  await page.getByLabel('Issue-IID').fill('1');
  await page.getByRole('button', { name: 'Sitzung erstellen' }).click();
  await expect(page.locator('.preview')).toContainText('#1 ·');
}
