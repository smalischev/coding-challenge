import { Inject, Injectable, InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { fetchEventSource, FetchEventSourceInit } from '@microsoft/fetch-event-source';
import { AllDevelopersEstimatedResponse, EstimateRequest, EstimationProgressResponse, PlanningPokerId, ScrumMasterRequest } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';
import { AuthTokenService } from '../../core/auth/auth-token.service';
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

@Injectable({ providedIn: 'root' })
export class EstimationService {
  constructor(
    private readonly api: PlanningPokerApiService,
    private readonly token: AuthTokenService,
    @Inject(FETCH_EVENT_SOURCE)
    private readonly fetchEventSource: (input: RequestInfo, init: FetchEventSourceInit) => Promise<void>,
    @Inject(PLANNING_POKER_API_BASE_URL)
    private readonly apiBaseUrl: string,
  ) {}

  submit(sessionId: PlanningPokerId, request: EstimateRequest): Observable<void> {
    return this.api.post<void>(`${this.roundPath(sessionId)}/estimates`, request);
  }

  getProgress(sessionId: PlanningPokerId): Observable<EstimationProgressResponse> {
    return this.api.get<EstimationProgressResponse>(`${this.roundPath(sessionId)}/progress`);
  }

  areAllDevelopersEstimated(sessionId: PlanningPokerId): Observable<AllDevelopersEstimatedResponse> {
    return this.api.get<AllDevelopersEstimatedResponse>(`${this.roundPath(sessionId)}/all-developers-estimated`);
  }

  reveal(sessionId: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.api.post<void>(`${this.roundPath(sessionId)}/reveal`, request);
  }

  startNewRound(sessionId: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.api.post<void>(this.roundPath(sessionId), request);
  }

  watchAllDevelopersEstimated(sessionId: PlanningPokerId): Observable<void> {
    return new Observable<void>((subscriber) => {
      const controller = new AbortController();
      const token = this.token.token();
      if (!token) {
        subscriber.error(new Error('No access token available for the SSE stream.'));
        return () => controller.abort();
      }
      void this.fetchEventSource(`${this.apiBaseUrl}${this.roundPath(sessionId)}/events`, {
        headers: { Authorization: `Bearer ${token}` },
        signal: controller.signal,
        onmessage: (event) => {
          if (event.event === 'all-developers-estimated') subscriber.next();
        },
        onclose: () => {
          if (!controller.signal.aborted) subscriber.complete();
        },
        onerror: (error) => {
          if (!controller.signal.aborted) subscriber.error(error);
          throw error;
        }
      }).catch((error: unknown) => {
        if (!controller.signal.aborted) subscriber.error(error);
      });
      return () => controller.abort();
    });
  }

  private roundPath(sessionId: PlanningPokerId): string { return `/planning-pokers/${encodeURIComponent(sessionId)}/active-round`; }
}
