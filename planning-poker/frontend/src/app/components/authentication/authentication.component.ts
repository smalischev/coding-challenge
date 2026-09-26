import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, Validators, FormControl, FormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize, Observable } from 'rxjs';
import { AuthSessionService } from '../../core/auth/auth-session.service';
import { ApiRole } from '../../core/api/planning-poker-api.models';

@Component({
  selector: 'app-authentication',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './authentication.component.html',
  styleUrl: './authentication.component.css'
})
export class AuthenticationComponent {
  readonly auth = inject(AuthSessionService);
  mode: 'login' | 'register' = 'login';
  loading = false;
  message = '';
  error = '';
  readonly form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(12)] }),
    role: new FormControl<ApiRole>('DEVELOPER', { nonNullable: true })
  });

  changeMode(mode: 'login' | 'register'): void {
    this.mode = mode;
    this.message = '';
    this.error = '';
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.error = '';
    const { username, password, role } = this.form.getRawValue();
    const request: Observable<unknown> = this.mode === 'register'
      ? this.auth.register({ username, password, role })
      : this.auth.login({ username, password });
    request.pipe(finalize(() => this.loading = false)).subscribe({
      next: () => {
        if (this.mode === 'register') {
          this.message = 'Account created. You can now sign in.';
          this.mode = 'login';
        }
      },
      error: (error: HttpErrorResponse) => this.error = error.error?.message ?? 'The request could not be completed.'
    });
  }
}
