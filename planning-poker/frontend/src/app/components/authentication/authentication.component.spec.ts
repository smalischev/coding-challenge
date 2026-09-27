import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { AuthenticationComponent } from './authentication.component';
import { PokerSessionStore } from '../../store/poker-session.store';

describe('AuthenticationComponent', () => {
  let fixture: ComponentFixture<AuthenticationComponent>;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [AuthenticationComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', component: AuthenticationComponent },
          { path: 'poker', component: AuthenticationComponent }
        ])
      ]
    });
    fixture = TestBed.createComponent(AuthenticationComponent);
    httpTesting = TestBed.inject(HttpTestingController);
    fixture.componentInstance.changeMode('register');
    fixture.componentInstance.form.setValue({
      username: 'test-developer',
      password: 'safe-test-password',
      role: 'DEVELOPER'
    });
    fixture.detectChanges();
  });

  afterEach(() => httpTesting.verify());

  it('sends a registration request when Create account is clicked', () => {
    const button = fixture.nativeElement.querySelector('button.submit') as HTMLButtonElement;
    button.click();

    const request = httpTesting.expectOne(`${environment.apiBaseUrl}/auth/register`);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      username: 'test-developer',
      password: 'safe-test-password',
      role: 'DEVELOPER'
    });
    request.flush(null);
  });

  it('resets a previous poker session before navigating after login', () => {
    const pokerSessionStore = TestBed.inject(PokerSessionStore);
    const resetSession = vi.spyOn(pokerSessionStore, 'resetSession');
    fixture.componentInstance.changeMode('login');

    (fixture.nativeElement.querySelector('button.submit') as HTMLButtonElement).click();

    const request = httpTesting.expectOne(`${environment.apiBaseUrl}/auth/login`);
    request.flush({
      accessToken: 'header.eyJncm91cHMiOlsiREVWRUxPUEVSIl19.signature',
      tokenType: 'Bearer',
      expiresIn: 3600,
    });

    expect(resetSession).toHaveBeenCalledOnce();
  });
});
