import { HttpHandlerFn, HttpInterceptorFn, HttpRequest, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { AuthStore } from './auth.store';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
) => {
  const authStore = inject(AuthStore);
  const router = inject(Router);
  const user = authStore.currentUser();

  if (!user) {
    return next(req);
  }

  const authReq = req.clone({
    setHeaders: { Authorization: `Bearer ${user.token}` },
  });

  return next(authReq).pipe(
    catchError((err) => {
      if (err?.status === HttpStatusCode.Unauthorized) {
        localStorage.removeItem('auth_token');
        authStore.clear();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    }),
  );
};
