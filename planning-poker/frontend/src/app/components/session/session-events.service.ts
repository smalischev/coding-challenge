import { Inject, Injectable, InjectionToken } from '@angular/core';
import { FetchEventSourceInit, fetchEventSource } from '@microsoft/fetch-event-source';
import { Observable } from 'rxjs';
import { AuthTokenService } from '../../core/auth/auth-token.service';
import { PlanningPokerId } from '../../core/api/planning-poker-api.models';
import { environment } from '../../../environments/environment';

export const FETCH_EVENT_SOURCE = new InjectionToken<
  (input: RequestInfo, init: FetchEventSourceInit) => Promise<void>
>('FETCH_EVENT_SOURCE', {
  providedIn: 'root',
  factory: () => fetchEventSource,
});

export const PLANNING_POKER_API_BASE_URL = new InjectionToken<string>('PLANNING_POKER_API_BASE_URL', {
  providedIn: 'root',
  factory: () => environment.apiBaseUrl,
});

export interface SessionEvent {
  name: string;
  data: unknown;
}

@Injectable({ providedIn: 'root' })
export class SessionEventsService {
  constructor(
    private readonly token: AuthTokenService,
    @Inject(FETCH_EVENT_SOURCE)
    private readonly fetchEventSource: (input: RequestInfo, init: FetchEventSourceInit) => Promise<void>,
    @Inject(PLANNING_POKER_API_BASE_URL)
    private readonly apiBaseUrl: string,
  ) {}

  watch(sessionId: PlanningPokerId): Observable<SessionEvent> {
    return new Observable<SessionEvent>((subscriber) => {
      const controller = new AbortController();
      const token = this.token.token();
      if (!token) {
        subscriber.error(new Error('No access token available for the SSE stream.'));
        return () => controller.abort();
      }
      void this.fetchEventSource(`${this.apiBaseUrl}/planning-pokers/${encodeURIComponent(sessionId)}/events`, {
        headers: { Authorization: `Bearer ${token}` },
        signal: controller.signal,
        onmessage: (event) => subscriber.next({ name: event.event, data: event.data ? JSON.parse(event.data) : {} }),
        onclose: () => { if (!controller.signal.aborted) subscriber.complete(); },
        onerror: (error) => {
          if (!controller.signal.aborted)
            subscriber.error(error);
          throw error;
        }
      }).catch((error: unknown) => {
        if (!controller.signal.aborted) subscriber.error(error);
      });
      return () => controller.abort();
    });
  }
}
