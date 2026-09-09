import { Injectable, computed, signal } from '@angular/core';

export interface CurrentUser {
  userId: string;
  displayName: string;
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly _currentUser = signal<CurrentUser | null>(null);

  readonly currentUser = computed(() => this._currentUser());
  readonly isAuthenticated = computed(() => this._currentUser() !== null);

  setUser(user: CurrentUser): void {
    this._currentUser.set(user);
  }

  clear(): void {
    this._currentUser.set(null);
  }
}
