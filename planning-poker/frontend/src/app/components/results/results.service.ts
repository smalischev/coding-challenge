import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BackendCardValue, EstimateValueResponse, FinalizeResultRequest, NumericEstimationResponse, PlanningPokerId } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';

@Injectable({ providedIn: 'root' })
export class ResultsService {
  constructor(private readonly api: PlanningPokerApiService) {}

  getEstimates(sessionId: PlanningPokerId): Observable<EstimateValueResponse[]> {
    return this.api.getEstimates(sessionId);
  }

  getGroups(sessionId: PlanningPokerId): Observable<Partial<Record<BackendCardValue, number>>> {
    return this.api.getEstimateGroups(sessionId);
  }

  getAverage(sessionId: PlanningPokerId): Observable<NumericEstimationResponse> {
    return this.api.getAverage(sessionId);
  }

  getMostFrequentValue(sessionId: PlanningPokerId): Observable<NumericEstimationResponse> {
    return this.api.getMostFrequentValue(sessionId);
  }

  finalize(sessionId: PlanningPokerId, request: FinalizeResultRequest): Observable<void> {
    return this.api.finalizeResult(sessionId, request);
  }
}
