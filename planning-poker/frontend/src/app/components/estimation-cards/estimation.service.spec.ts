import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { AuthTokenService } from '../../core/auth/auth-token.service';
import { AuthenticationService } from '../authentication/authentication.service';
import { FETCH_EVENT_SOURCE, PLANNING_POKER_API_BASE_URL, SessionEventsService } from '../session/session-events.service';

const fetchEventSourceMock = vi.fn();

describe('SessionEventsService', () => {
  let service: SessionEventsService;
  let authToken: AuthTokenService;

  beforeEach(() => {
    fetchEventSourceMock.mockReset();
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });
    TestBed.overrideProvider(FETCH_EVENT_SOURCE, { useValue: fetchEventSourceMock });
    TestBed.overrideProvider(PLANNING_POKER_API_BASE_URL, { useValue: environment.apiBaseUrl });
    TestBed.overrideProvider(AuthenticationService, { useValue: { clearSession: vi.fn() } });
    TestBed.overrideProvider(Router, { useValue: { navigate: vi.fn() } });

    service = TestBed.inject(SessionEventsService);
    authToken = TestBed.inject(AuthTokenService);
  });

  it('emits the completion SSE event with its payload', () => {
    authToken.set('test-jwt');
    fetchEventSourceMock.mockImplementation((_url, options) => {
      options.onmessage({ event: 'all-developers-estimated', data: '{}' });
      return Promise.resolve();
    });
    const notification = vi.fn();

    const subscription = service.watch('session-id').subscribe(notification);

    expect(fetchEventSourceMock).toHaveBeenCalledWith(
      environment.apiBaseUrl + '/planning-pokers/session-id/events',
      expect.objectContaining({
        headers: { Authorization: 'Bearer test-jwt' },
      }),
    );
    expect(notification).toHaveBeenCalledWith({ name: 'all-developers-estimated', data: {} });

    subscription.unsubscribe();
  });
});
