import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthenticationService, AuthenticatedUser } from '../../components/authentication/authentication.service';
import { authGuard } from './auth.guard';

describe('authGuard', () => {
  const user = signal<AuthenticatedUser | null>(null);
  const loginUrlTree = { redirect: '/login' };
  const router = { createUrlTree: vi.fn(() => loginUrlTree) };

  beforeEach(() => {
    user.set(null);
    router.createUrlTree.mockClear();
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthenticationService, useValue: { user } },
        { provide: Router, useValue: router }
      ]
    });
  });

  it('allows access to the poker route for an authenticated user', () => {
    user.set({ username: 'Anna', role: 'SCRUM_MASTER' });

    const result = TestBed.runInInjectionContext(() => authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

    expect(result).toBe(true);
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  it('redirects an unauthenticated user to login', () => {
    const result = TestBed.runInInjectionContext(() => authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

    expect(result).toBe(loginUrlTree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });
});
