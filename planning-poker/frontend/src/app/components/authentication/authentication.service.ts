import { Injectable, signal } from '@angular/core';
import { Observable, finalize, map, of, tap } from 'rxjs';
import { ApiRole, Credentials, TokenResponse } from '../../core/api/planning-poker-api.models';
import { PlanningPokerApiService } from '../../core/api/planning-poker-api.service';
import { AuthTokenService } from '../../core/auth/auth-token.service';

export interface AuthenticatedUser {
  username: string;
  role: ApiRole;
}

interface StoredSession {
  accessToken: string;
  user: AuthenticatedUser;
}

@Injectable({ providedIn: 'root' })
export class AuthenticationService {
  private readonly storageKey = 'planning-poker.session';
  readonly user = signal<AuthenticatedUser | null>(this.readStoredSession()?.user ?? null);

  constructor(private readonly api: PlanningPokerApiService, private readonly token: AuthTokenService) {
    this.token.set(this.readStoredSession()?.accessToken ?? null);
  }

  register(credentials: Required<Credentials>): Observable<void> {
    return this.api.post<void>('/auth/register', credentials);
  }

  login(credentials: Pick<Credentials, 'username' | 'password'>): Observable<AuthenticatedUser> {
    return this.api.post<TokenResponse>('/auth/login', credentials).pipe(
      tap((response) => this.token.set(response.accessToken)),
      map((response) => {
        const user: AuthenticatedUser = { username: credentials.username, role: this.roleFromToken(response.accessToken) };
        this.user.set(user);
        this.storeSession({ accessToken: response.accessToken, user });
        return user;
      })
    );
  }

  logout(): Observable<void> {
    if (!this.user()) {
      return of(void 0);
    }
    return this.api.post<void>('/auth/logout', {}).pipe(finalize(() => this.clearSession()));
  }

  private readStoredSession(): StoredSession | null {
    const serializedSession = sessionStorage.getItem(this.storageKey);
    if (!serializedSession) {
      return null;
    }
    try {
      return JSON.parse(serializedSession) as StoredSession;
    } catch {
      sessionStorage.removeItem(this.storageKey);
      return null;
    }
  }

  private storeSession(session: StoredSession): void {
    sessionStorage.setItem(this.storageKey, JSON.stringify(session));
  }

  /** Removes locally stored credentials without calling the backend. */
  clearSession(): void {
    this.token.set(null);
    this.user.set(null);
    sessionStorage.removeItem(this.storageKey);
  }

  private roleFromToken(token: string): ApiRole {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))) as { groups?: ApiRole[] };
    return payload.groups?.[0] ?? 'DEVELOPER';
  }
}
