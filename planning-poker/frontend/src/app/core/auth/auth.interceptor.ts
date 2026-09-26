import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthTokenService } from './auth-token.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(AuthTokenService).token();
  return next(token ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request);
};
