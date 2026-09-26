import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BackendCardValue, EstimateValueResponse, FinalizeResultRequest, NumericEstimationResponse, PlanningPokerId } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';

@Injectable({ providedIn: 'root' })
export class ResultsService {
  constructor(private readonly api: PlanningPokerApiService) {}

  getEstimates(sessionId: PlanningPokerId): Observable<EstimateValueResponse[]> {
    return this.api.get<EstimateValueResponse[]>(`${this.roundPath(sessionId)}/estimates`);
  }

  getGroups(sessionId: PlanningPokerId): Observable<Partial<Record<BackendCardValue, number>>> {
    return this.api.get<Partial<Record<BackendCardValue, number>>>(`${this.roundPath(sessionId)}/estimate-groups`);
  }

  getAverage(sessionId: PlanningPokerId): Observable<NumericEstimationResponse> {
    return this.api.get<NumericEstimationResponse>(`${this.roundPath(sessionId)}/average`);
  }

  getMostFrequentValue(sessionId: PlanningPokerId): Observable<NumericEstimationResponse> {
    return this.api.get<NumericEstimationResponse>(`${this.roundPath(sessionId)}/most-frequent-value`);
  }

  finalize(sessionId: PlanningPokerId, request: FinalizeResultRequest): Observable<void> {
    return this.api.post<void>(`${this.roundPath(sessionId)}/result`, request);
  }

  private roundPath(sessionId: PlanningPokerId): string { return `/planning-pokers/${encodeURIComponent(sessionId)}/active-round`; }
}
