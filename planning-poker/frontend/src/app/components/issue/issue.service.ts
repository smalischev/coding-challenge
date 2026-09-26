import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ActiveIssueResponse, PlanningPokerId, ScrumMasterRequest, SelectIssueRequest } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';

@Injectable({ providedIn: 'root' })
export class IssueService {
  constructor(private readonly api: PlanningPokerApiService) {}

  select(sessionId: PlanningPokerId, request: SelectIssueRequest): Observable<void> {
    return this.api.selectActiveIssue(sessionId, request);
  }

  getActive(sessionId: PlanningPokerId): Observable<ActiveIssueResponse> {
    return this.api.getActiveIssue(sessionId);
  }

  release(sessionId: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.api.releaseActiveIssue(sessionId, request);
  }
}
