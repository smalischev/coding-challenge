import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { AuthenticationService } from '../authentication/authentication.service';
import { IssueService } from '../issue/issue.service';
import { SessionService } from '../session/session.service';
import { authInterceptor } from '../../core/auth/auth.interceptor';
import { AuthTokenService } from '../../core/auth/auth-token.service';
import { EstimationService } from './estimation.service';

const processEnvironment =
  (globalThis as typeof globalThis & {
    process?: {
      env?: Record<string, string | undefined>;
      getBuiltinModule?: (name: string) => { transferableAbortController: typeof AbortController } | undefined;
    };
  }).process;
const describeIntegration = processEnvironment?.env?.['RUN_BACKEND_INTEGRATION'] === 'true' ? describe : describe.skip;
const nativeAbortController = globalThis.AbortController;

describeIntegration('EstimationService backend integration', () => {
  let authentication: AuthenticationService;
  let authToken: AuthTokenService;
  let sessions: SessionService;
  let issues: IssueService;
  let estimations: EstimationService;

  beforeAll(() => {
    // JSDOM replaces AbortController; the Microsoft SSE client needs Node's native variant for fetch.
    const nodeUtil = processEnvironment?.getBuiltinModule?.('node:util');
    if (!nodeUtil) {
      throw new Error('This integration test requires Node.js with process.getBuiltinModule().');
    }
    globalThis.AbortController = nodeUtil.transferableAbortController;
  });

  afterAll(() => {
    globalThis.AbortController = nativeAbortController;
  });

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(withFetch(), withInterceptors([authInterceptor]))],
    });
    authentication = TestBed.inject(AuthenticationService);
    authToken = TestBed.inject(AuthTokenService);
    sessions = TestBed.inject(SessionService);
    issues = TestBed.inject(IssueService);
    estimations = TestBed.inject(EstimationService);
  });

  it(
    'opens an SSE stream and receives the completion event from the backend',
    async () => {
      const suffix = `${Date.now()}-${Math.random().toString(36).slice(2)}`;
      const scrumMaster = await createUser(`SseMaster-${suffix}`, 'SCRUM_MASTER');
      const developerOne = await createUser(`SseDeveloperOne-${suffix}`, 'DEVELOPER');
      const developerTwo = await createUser(`SseDeveloperTwo-${suffix}`, 'DEVELOPER');

      authToken.set(scrumMaster.token);
      const session = await firstValueFrom(sessions.create({
        scrumMasterName: scrumMaster.username,
        gitlabProjectId: 1,
        gitlabIssueIid: 1,
      }));
      authToken.set(developerOne.token);
      await firstValueFrom(sessions.join(session.planningPokerId, { developerName: developerOne.username }));
      authToken.set(developerTwo.token);
      await firstValueFrom(sessions.join(session.planningPokerId, { developerName: developerTwo.username }));
      authToken.set(scrumMaster.token);
      await firstValueFrom(issues.release(session.planningPokerId, { scrumMasterName: scrumMaster.username }));

      const completion = firstValueFrom(estimations.watchAllDevelopersEstimated(session.planningPokerId));
      await waitForConnection();
      authToken.set(developerOne.token);
      await firstValueFrom(estimations.submit(session.planningPokerId, { developerName: developerOne.username, value: 'FIVE' }));
      authToken.set(developerTwo.token);
      await firstValueFrom(estimations.submit(session.planningPokerId, { developerName: developerTwo.username, value: 'FIVE' }));

      await expect(completion).resolves.toBeUndefined();
    },
    10_000,
  );

  async function createUser(username: string, role: 'SCRUM_MASTER' | 'DEVELOPER'): Promise<TestUser> {
    const password = 'a-secure-test-password';
    await firstValueFrom(authentication.register({ username, password, role }));
    await firstValueFrom(authentication.login({ username, password }));
    const token = authToken.token();
    if (!token) {
      throw new Error('Login did not provide an access token.');
    }
    return { username, token };
  }
});

interface TestUser {
  username: string;
  token: string;
}

function waitForConnection(): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, 250));
}
