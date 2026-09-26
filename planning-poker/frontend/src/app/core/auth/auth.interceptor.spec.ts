import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { PlanningPokerApiService } from '../api/planning-poker-api.service';
import { authInterceptor } from './auth.interceptor';
import { AuthTokenService } from './auth-token.service';

describe('authInterceptor', () => {
  let api: PlanningPokerApiService;
  let token: AuthTokenService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting()] });
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
});
