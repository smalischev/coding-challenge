import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { AuthTokenService } from '../../core/auth/auth-token.service';
import { EstimationService, FETCH_EVENT_SOURCE, PLANNING_POKER_API_BASE_URL } from './estimation.service';

const fetchEventSourceMock = vi.fn();

describe('EstimationService', () => {
  let service: EstimationService;
  let authToken: AuthTokenService;

  beforeEach(() => {
    fetchEventSourceMock.mockReset();
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });
    TestBed.overrideProvider(FETCH_EVENT_SOURCE, { useValue: fetchEventSourceMock });
    TestBed.overrideProvider(PLANNING_POKER_API_BASE_URL, { useValue: environment.apiBaseUrl });

    service = TestBed.inject(EstimationService);
    authToken = TestBed.inject(AuthTokenService);
  });

  it('notifies the scrum master when the completion SSE event arrives', () => {
    authToken.set('test-jwt');
    fetchEventSourceMock.mockImplementation((_url, options) => {
      options.onmessage({ event: 'all-developers-estimated', data: '{}' });
      return Promise.resolve();
    });
    const notification = vi.fn();

    const subscription = service.watchAllDevelopersEstimated('session-id').subscribe(notification);

    expect(fetchEventSourceMock).toHaveBeenCalledWith(
      environment.apiBaseUrl + '/planning-pokers/session-id/active-round/events',
      expect.objectContaining({
        headers: { Authorization: 'Bearer test-jwt' },
      }),
    );
    expect(notification).toHaveBeenCalledOnce();

    subscription.unsubscribe();
  });
});
