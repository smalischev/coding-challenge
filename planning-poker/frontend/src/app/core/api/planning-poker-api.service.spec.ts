import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../environments/environment';
import { PlanningPokerApiService } from './planning-poker-api.service';

describe('PlanningPokerApiService', () => {
  let api: PlanningPokerApiService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    api = TestBed.inject(PlanningPokerApiService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('sends a POST request to the given path', () => {
    const credentials = { username: 'test-developer', password: 'safe-test-password', role: 'DEVELOPER' as const };

    api.post<void>('/auth/register', credentials).subscribe();

    const request = httpTesting.expectOne(`${environment.apiBaseUrl}/auth/register`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(credentials);
    request.flush(null);
  });
});
