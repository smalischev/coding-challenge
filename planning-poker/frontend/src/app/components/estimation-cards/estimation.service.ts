import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { AllDevelopersEstimatedResponse, EstimateRequest, EstimationProgressResponse, PlanningPokerId, ScrumMasterRequest } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';

export class EstimationRoundRevealedError extends Error {
  constructor() {
    super('estimates cannot be changed after reveal');
  }
}

@Injectable({ providedIn: 'root' })
export class EstimationService {
  constructor(private readonly api: PlanningPokerApiService) {}

  submit(sessionId: PlanningPokerId, request: EstimateRequest): Observable<void> {
    return this.api.post<void>(`${this.roundPath(sessionId)}/estimates`, request).pipe(
      catchError((error: unknown) => {
        if (error instanceof HttpErrorResponse && error.error?.message === 'estimates cannot be changed after reveal') {
          return throwError(() => new EstimationRoundRevealedError());
        }
        return throwError(() => error);
      }),
    );
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

  private roundPath(sessionId: PlanningPokerId): string { return `/planning-pokers/${encodeURIComponent(sessionId)}/active-round`; }
}
