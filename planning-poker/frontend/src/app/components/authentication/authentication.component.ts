import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, Validators, FormControl, FormGroup } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize, Observable } from 'rxjs';
import { AuthenticationService } from './authentication.service';
import { ApiRole } from '../../core/api/planning-poker-api.models';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-authentication',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './authentication.component.html',
  styleUrl: './authentication.component.css'
})
export class AuthenticationComponent {
  readonly auth = inject(AuthenticationService);
  readonly environment = environment;
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  mode: 'login' | 'register' = this.route.snapshot.data['mode'] === 'register' ? 'register' : 'login';
  loading = false;
  submitted = false;
  message = '';
  error = '';
  readonly form = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(12)] }),
    role: new FormControl<ApiRole>('DEVELOPER', { nonNullable: true })
  });

  changeMode(mode: 'login' | 'register'): void {
    this.mode = mode;
    this.submitted = false;
    this.message = '';
    this.error = '';
  }

  continueWithDemoSession(): void {
    this.auth.startDemoSession();
    this.router.navigate(['/poker']);
  }

  submit(): void {
    this.submitted = true;
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
          this.router.navigate(['/login']);
        } else {
          this.router.navigate(['/poker']);
        }
      },
      error: (error: HttpErrorResponse) => this.error = error.error?.message ?? 'The request could not be completed.'
    });
  }
}
