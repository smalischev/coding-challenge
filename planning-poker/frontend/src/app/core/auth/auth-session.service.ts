import { Injectable, signal } from '@angular/core';
import { Observable, finalize, map, of, tap } from 'rxjs';
import { ApiRole, Credentials } from '../api/planning-poker-api.models';
import { PlanningPokerApiService } from '../api/planning-poker-api.service';

export interface AuthenticatedUser {
  username: string;
  role: ApiRole;
}

interface StoredSession {
  accessToken: string;
  user: AuthenticatedUser;
}

@Injectable({ providedIn: 'root' })
export class AuthSessionService {
  private readonly storageKey = 'planning-poker.session';
  readonly user = signal<AuthenticatedUser | null>(this.readStoredSession()?.user ?? null);

  constructor(private readonly api: PlanningPokerApiService) {
    this.api.setAccessToken(this.readStoredSession()?.accessToken ?? null);
  }

  register(credentials: Required<Credentials>): Observable<void> {
    return this.api.register(credentials);
  }

  login(credentials: Pick<Credentials, 'username' | 'password'>): Observable<AuthenticatedUser> {
    return this.api.login(credentials).pipe(
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
    return this.api.logout().pipe(finalize(() => this.clearSession()));
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

  private clearSession(): void {
    this.api.setAccessToken(null);
    this.user.set(null);
    sessionStorage.removeItem(this.storageKey);
  }

  private roleFromToken(token: string): ApiRole {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))) as { groups?: ApiRole[] };
    return payload.groups?.[0] ?? 'DEVELOPER';
  }
}
