import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../environments/environment';
import { SessionHistoryService } from './session-history.service';

describe('SessionHistoryService', () => {
  const sessionId = '74f406ad-6696-4fba-926f-e1d081e926a9';
  let service: SessionHistoryService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(SessionHistoryService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('loads archived rounds for a session', () => {
    service.getCompletedRounds(sessionId).subscribe();

    const request = httpTesting.expectOne(`${environment.apiBaseUrl}/planning-pokers/${sessionId}/completed-rounds`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });
});
