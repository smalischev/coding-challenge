import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/** Shared HTTP transport only. Feature services own their endpoint paths and DTOs. */
@Injectable({ providedIn: 'root' })
export class PlanningPokerApiService {
  constructor(private readonly http: HttpClient) {}

  get<T>(path: string): Observable<T> { return this.http.get<T>(this.url(path)); }
  post<T>(path: string, body: unknown): Observable<T> { return this.http.post<T>(this.url(path), body); }
  put<T>(path: string, body: unknown): Observable<T> { return this.http.put<T>(this.url(path), body); }

  private url(path: string): string {
    return `${environment.apiBaseUrl}${path}`;
  }
}
