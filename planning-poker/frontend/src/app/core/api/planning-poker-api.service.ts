import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ActiveIssueResponse, AllDevelopersEstimatedResponse, BackendCardValue, CreateSessionRequest,
  CreateSessionResponse, Credentials, DeveloperRequest, EstimateRequest, EstimateValueResponse,
  EstimationProgressResponse, FinalizeResultRequest, NumericEstimationResponse, PlanningPokerId,
  ScrumMasterRequest, SelectIssueRequest, TokenResponse
} from './planning-poker-api.models';

@Injectable({ providedIn: 'root' })
export class PlanningPokerApiService {
  private readonly baseUrl = environment.apiBaseUrl;
  private readonly accessToken = signal<string | null>(null);

  constructor(private readonly http: HttpClient) {}

  register(credentials: Credentials): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/register`, credentials);
  }

  login(credentials: Credentials): Observable<TokenResponse> {
    return this.http.post<TokenResponse>(`${this.baseUrl}/auth/login`, credentials).pipe(
      tap((response) => this.accessToken.set(response.accessToken))
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/auth/logout`, {}, this.authorizedOptions()).pipe(
      tap(() => this.accessToken.set(null))
    );
  }

  createSession(request: CreateSessionRequest): Observable<CreateSessionResponse> {
    return this.http.post<CreateSessionResponse>(`${this.baseUrl}/planning-pokers`, request, this.authorizedOptions());
  }

  joinSession(id: PlanningPokerId, request: DeveloperRequest): Observable<void> {
    return this.http.post<void>(`${this.sessionUrl(id)}/developers`, request, this.authorizedOptions());
  }

  selectActiveIssue(id: PlanningPokerId, request: SelectIssueRequest): Observable<void> {
    return this.http.put<void>(`${this.sessionUrl(id)}/active-issue`, request, this.authorizedOptions());
  }

  getActiveIssue(id: PlanningPokerId): Observable<ActiveIssueResponse> {
    return this.http.get<ActiveIssueResponse>(`${this.sessionUrl(id)}/active-issue`, this.authorizedOptions());
  }

  releaseActiveIssue(id: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.http.post<void>(`${this.sessionUrl(id)}/active-issue/release`, request, this.authorizedOptions());
  }

  submitEstimate(id: PlanningPokerId, request: EstimateRequest): Observable<void> {
    return this.http.post<void>(`${this.roundUrl(id)}/estimates`, request, this.authorizedOptions());
  }

  getProgress(id: PlanningPokerId): Observable<EstimationProgressResponse> {
    return this.http.get<EstimationProgressResponse>(`${this.roundUrl(id)}/progress`, this.authorizedOptions());
  }

  getAllDevelopersEstimated(id: PlanningPokerId): Observable<AllDevelopersEstimatedResponse> {
    return this.http.get<AllDevelopersEstimatedResponse>(`${this.roundUrl(id)}/all-developers-estimated`, this.authorizedOptions());
  }

  revealRound(id: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.http.post<void>(`${this.roundUrl(id)}/reveal`, request, this.authorizedOptions());
  }

  getEstimates(id: PlanningPokerId): Observable<EstimateValueResponse[]> {
    return this.http.get<EstimateValueResponse[]>(`${this.roundUrl(id)}/estimates`, this.authorizedOptions());
  }

  getEstimateGroups(id: PlanningPokerId): Observable<Partial<Record<BackendCardValue, number>>> {
    return this.http.get<Partial<Record<BackendCardValue, number>>>(`${this.roundUrl(id)}/estimate-groups`, this.authorizedOptions());
  }

  getAverage(id: PlanningPokerId): Observable<NumericEstimationResponse> {
    return this.http.get<NumericEstimationResponse>(`${this.roundUrl(id)}/average`, this.authorizedOptions());
  }

  getMostFrequentValue(id: PlanningPokerId): Observable<NumericEstimationResponse> {
    return this.http.get<NumericEstimationResponse>(`${this.roundUrl(id)}/most-frequent-value`, this.authorizedOptions());
  }

  startNewRound(id: PlanningPokerId, request: ScrumMasterRequest): Observable<void> {
    return this.http.post<void>(this.roundUrl(id), request, this.authorizedOptions());
  }

  finalizeResult(id: PlanningPokerId, request: FinalizeResultRequest): Observable<void> {
    return this.http.post<void>(`${this.roundUrl(id)}/result`, request, this.authorizedOptions());
  }

  private sessionUrl(id: PlanningPokerId): string {
    return `${this.baseUrl}/planning-pokers/${encodeURIComponent(id)}`;
  }

  private roundUrl(id: PlanningPokerId): string {
    return `${this.sessionUrl(id)}/active-round`;
  }

  private authorizedOptions(): { headers: HttpHeaders } {
    const token = this.accessToken();
    return { headers: token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders() };
  }
}
