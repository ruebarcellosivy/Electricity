import { HttpInterceptorFn } from '@angular/common/http';

/** Ensures the session cookie (and CSRF cookie) travel with every API request. */
export const credentialsInterceptor: HttpInterceptorFn = (req, next) => {
  return next(req.clone({ withCredentials: true }));
};
