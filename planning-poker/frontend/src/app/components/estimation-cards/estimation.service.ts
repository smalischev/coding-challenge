import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AllDevelopersEstimatedResponse, EstimateRequest, EstimationProgressResponse, PlanningPokerId, ScrumMasterRequest } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';

@Injectable({ providedIn: 'root' })
export class EstimationService {
  constructor(private readonly api: PlanningPokerApiService) {}

  submit(sessionId: PlanningPokerId, request: EstimateRequest): Observable<void> {
    return this.api.submitEstimate(sessionId, request);
  }

  getProgress(sessionId: PlanningPokerId): Observable<EstimationProgressResponse> {
    return this.api.getProgress(sessionId);
  }

  areAllDevelopersEstimated(sessionId: PlanningPokerId): Observable<AllDevelopersEstimatedResponse> {
    return this.api.getAllDevelopersEstimated(sessionId);
  }

  reveal(sessionId: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.api.revealRound(sessionId, request);
  }

  startNewRound(sessionId: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.api.startNewRound(sessionId, request);
  }
}
