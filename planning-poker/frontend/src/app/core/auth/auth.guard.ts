import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthenticationService } from '../../components/authentication/authentication.service';

export const authGuard: CanActivateFn = () => {
  const authentication = inject(AuthenticationService);
  const router = inject(Router);

  return authentication.user() ? true : router.createUrlTree(['/login']);
};
