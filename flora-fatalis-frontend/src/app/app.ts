import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from './auth/auth.service';
import { AuthStore } from './auth/auth.store';
import { HouseholdStore } from './household/household.store';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly authService = inject(AuthService);
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  protected readonly householdStore = inject(HouseholdStore);

  protected readonly title = 'Flora Fatalis';

  protected readonly isAuthenticated = this.authStore.isAuthenticated;

  protected readonly displayName = computed(() => this.authStore.currentUser()?.displayName ?? null);

  protected readonly initials = computed(() => {
    const name = this.displayName();
    if (!name) return null;
    return name
      .split(' ')
      .slice(0, 2)
      .map((part) => part.charAt(0).toUpperCase())
      .join('');
  });

  protected readonly userMenuOpen = signal(false);

  toggleUserMenu(): void {
    this.userMenuOpen.update((v) => !v);
  }

  closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
