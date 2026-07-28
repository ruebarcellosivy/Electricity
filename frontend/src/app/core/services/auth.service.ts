import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  ChangePasswordRequest,
  LoginRequest,
  LoginResponse,
  MessageResponse,
  RegisterRequest,
  RegisterResponse
} from '../models/auth.model';

const STORAGE_KEY = 'ebs_current_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = '/api/auth';

  /** Reactive signal holding the current session; null when logged out. */
  readonly currentUser = signal<LoginResponse | null>(this.readStoredUser());

  constructor(private readonly http: HttpClient) {}

  private readStoredUser(): LoginResponse | null {
    const raw = sessionStorage.getItem(STORAGE_KEY);
    return raw ? (JSON.parse(raw) as LoginResponse) : null;
  }

  private storeUser(user: LoginResponse | null): void {
    if (user) {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    } else {
      sessionStorage.removeItem(STORAGE_KEY);
    }
    this.currentUser.set(user);
  }

  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, request);
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => this.storeUser(response))
    );
  }

  logout(): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/logout`, {}).pipe(
      tap(() => this.storeUser(null))
    );
  }

  me(): Observable<LoginResponse> {
    return this.http.get<LoginResponse>(`${this.baseUrl}/me`).pipe(
      tap((response) => this.storeUser(response))
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/change-password`, request).pipe(
      tap(() => {
        const user = this.currentUser();
        if (user) {
          this.storeUser({ ...user, mustChangePassword: false });
        }
      })
    );
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }

  hasRole(role: string): boolean {
    return this.currentUser()?.role === role;
  }

  clearSession(): void {
    this.storeUser(null);
  }
}
