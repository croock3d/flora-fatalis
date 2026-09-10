import { HttpHandlerFn, HttpInterceptorFn, HttpRequest, HttpStatusCode } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { HouseholdStore } from '../household/household.store';
import { AUTH_TOKEN_KEY } from './auth.service';
import { AuthStore } from './auth.store';

export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
) => {
  const authStore = inject(AuthStore);
  const householdStore = inject(HouseholdStore);
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
        localStorage.removeItem(AUTH_TOKEN_KEY);
        authStore.clear();
        householdStore.clear();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    }),
  );
};
