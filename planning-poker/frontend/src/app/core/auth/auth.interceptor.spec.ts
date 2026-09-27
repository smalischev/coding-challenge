import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthenticationService } from '../../components/authentication/authentication.service';
import { PlanningPokerApiService } from '../api/planning-poker-api.service';
import { authInterceptor } from './auth.interceptor';
import { AuthTokenService } from './auth-token.service';

describe('authInterceptor', () => {
  let api: PlanningPokerApiService;
  let token: AuthTokenService;
  let httpTesting: HttpTestingController;
  let authentication: { clearSession: ReturnType<typeof vi.fn> };
  let router: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authentication = { clearSession: vi.fn() };
    router = { navigate: vi.fn().mockResolvedValue(true) };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthenticationService, useValue: authentication },
        { provide: Router, useValue: router },
      ],
    });
    api = TestBed.inject(PlanningPokerApiService);
    token = TestBed.inject(AuthTokenService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('adds the stored JWT as a bearer token', () => {
    token.set('test-jwt');
    api.get<void>('/protected').subscribe();
    const request = httpTesting.expectOne((candidate) => candidate.url.endsWith('/protected'));
    expect(request.request.headers.get('Authorization')).toBe('Bearer test-jwt');
    request.flush(null);
  });

  it('clears the session and redirects to login when an authenticated request returns 401', () => {
    token.set('expired-jwt');
    api.get<void>('/protected').subscribe({ error: () => undefined });
    const request = httpTesting.expectOne((candidate) => candidate.url.endsWith('/protected'));
    request.flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(authentication.clearSession).toHaveBeenCalledOnce();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
