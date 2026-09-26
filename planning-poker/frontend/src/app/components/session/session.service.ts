import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateSessionRequest, CreateSessionResponse, DeveloperRequest, PlanningPokerId } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';

@Injectable({ providedIn: 'root' })
export class SessionService {
  constructor(private readonly api: PlanningPokerApiService) {}

  create(request: CreateSessionRequest): Observable<CreateSessionResponse> {
    return this.api.createSession(request);
  }

  join(sessionId: PlanningPokerId, request: DeveloperRequest): Observable<void> {
    return this.api.joinSession(sessionId, request);
  }
}
