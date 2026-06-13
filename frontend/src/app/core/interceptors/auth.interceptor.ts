import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/** Sends cookies (Cloudflare Access session) with every API call and routes 401s to the login page. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req.clone({ withCredentials: true })).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !router.url.startsWith('/login')) {
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
