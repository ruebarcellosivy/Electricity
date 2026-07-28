import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ErrorResponse } from '../models/page-response.model';

const AUTH_ENDPOINTS = ['/api/auth/login', '/api/auth/register'];

/** Surfaces every failed API call as a friendly snackbar message and handles session expiry. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);
  const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const body = error.error as ErrorResponse | undefined;
      const message = body?.message || 'Something went wrong. Please try again.';

      if (error.status === 401 && !AUTH_ENDPOINTS.some((url) => req.url.includes(url))) {
        authService.clearSession();
        router.navigate(['/login']);
      }

      snackBar.open(message, 'Close', { duration: 5000, panelClass: 'snackbar-error' });
      return throwError(() => error);
    })
  );
};
