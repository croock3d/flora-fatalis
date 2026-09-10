import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap, tap } from 'rxjs';

import { HouseholdStore } from '../household/household.store';
import { AuthResponse, LoginRequest, RegisterRequest } from './auth.dto';
import { AuthStore } from './auth.store';

export const AUTH_TOKEN_KEY = 'auth_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authStore = inject(AuthStore);
  private readonly householdStore = inject(HouseholdStore);

  constructor() {
    this.restoreSession();
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', request).pipe(
      tap((response) => this.handleAuthResponse(response)),
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<void>('/api/auth/register', request).pipe(
      switchMap(() => this.login({ email: request.email, password: request.password })),
    );
  }

  logout(): void {
    localStorage.removeItem(AUTH_TOKEN_KEY);
    this.authStore.clear();
    this.householdStore.clear();
  }

  private handleAuthResponse(response: AuthResponse): void {
    localStorage.setItem(AUTH_TOKEN_KEY, response.token);
    this.authStore.setUser({
      userId: response.userId,
      displayName: response.displayName,
      token: response.token,
    });
    this.householdStore.load();
  }

  private restoreSession(): void {
    const token = localStorage.getItem(AUTH_TOKEN_KEY);
    if (!token) return;

    try {
      const payload = this.parseJwtPayload(token);
      const exp = payload['exp'];
      if (exp && Date.now() / 1000 > Number(exp)) {
        localStorage.removeItem(AUTH_TOKEN_KEY);
        return;
      }
      this.authStore.setUser({
        userId: String(payload['sub'] ?? ''),
        displayName: String(payload['displayName'] ?? ''),
        token,
      });
      this.householdStore.load();
    } catch {
      localStorage.removeItem(AUTH_TOKEN_KEY);
    }
  }

  private parseJwtPayload(token: string): Record<string, unknown> {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
    const json = new TextDecoder('utf-8').decode(bytes);
    return JSON.parse(json);
  }
}
