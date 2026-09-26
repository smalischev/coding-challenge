import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthTokenService {
  readonly token = signal<string | null>(null);
  set(token: string | null): void { this.token.set(token); }
}
