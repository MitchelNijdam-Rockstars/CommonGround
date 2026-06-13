import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Auth } from '../services/auth';

export const adminGuard: CanActivateFn = async () => {
  const auth = inject(Auth);
  const router = inject(Router);

  const user = await auth.load();
  if (!user) return router.createUrlTree(['/login']);
  return user.role === 'ADMIN' ? true : router.createUrlTree(['/']);
};
