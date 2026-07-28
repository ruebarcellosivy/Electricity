import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Restricts a route to the roles listed in its `data.roles` array. */
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = route.data['roles'] as string[] | undefined;

  const user = authService.currentUser();
  if (!user) {
    return router.parseUrl('/login');
  }
  if (!allowedRoles || allowedRoles.includes(user.role)) {
    return true;
  }
  return router.parseUrl('/unauthorized');
};
