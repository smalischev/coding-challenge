import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../environments/environment';
import { AuthenticationComponent } from './authentication.component';

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
});
