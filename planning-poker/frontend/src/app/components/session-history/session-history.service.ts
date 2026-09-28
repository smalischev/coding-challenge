import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CompletedRoundResponse, PlanningPokerId } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';

@Injectable({ providedIn: 'root' })
export class SessionHistoryService {
  constructor(private readonly api: PlanningPokerApiService) {}

  getCompletedRounds(sessionId: PlanningPokerId): Observable<CompletedRoundResponse[]> {
    return this.api.get<CompletedRoundResponse[]>(`/planning-pokers/${encodeURIComponent(sessionId)}/completed-rounds`);
  }
}
