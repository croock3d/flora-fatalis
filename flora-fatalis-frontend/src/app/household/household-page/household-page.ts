import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

import { HouseholdApiService } from '../household-api.service';
import { HouseholdStore } from '../household.store';

@Component({
  selector: 'app-household-page',
  imports: [FormsModule],
  templateUrl: './household-page.html',
  styleUrl: './household-page.css',
})
export class HouseholdPage {
  private readonly api = inject(HouseholdApiService);
  private readonly router = inject(Router);
  protected readonly store = inject(HouseholdStore);

  protected readonly inviteEmail = signal('');
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly pendingLeaveId = signal<string | null>(null);

  protected readonly canInvite = computed(() => {
    const owned = this.store.status()?.households.find((household) => household.ownedByMe);
    return !!owned && !owned.hasPartner && !this.store.status()?.outgoingInvitation;
  });

  protected sendInvitation(): void {
    const email = this.inviteEmail().trim();
    if (!email) return;

    this.loading.set(true);
    this.error.set(null);
    this.successMessage.set(null);

    this.api.sendInvitation(email).subscribe({
      next: () => {
        this.successMessage.set('Zaproszenie wysłane!');
        this.store.load();
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        if (err.status === 409) {
          this.error.set('To gospodarstwo ma już partnera albo zaproszenie');
        } else if (err.status === 404 || err.status === 400) {
          this.error.set('Nie znaleziono użytkownika o podanym emailu');
        } else {
          this.error.set('Nie udało się wysłać zaproszenia');
        }
      },
    });
  }

  protected accept(): void {
    const id = this.store.status()?.incomingInvitation?.invitationId;
    if (!id) return;
    this.run(() => this.api.accept(id));
  }

  protected reject(): void {
    const id =
      this.store.status()?.incomingInvitation?.invitationId ??
      this.store.status()?.outgoingInvitation?.invitationId;
    if (!id) return;
    this.run(() => this.api.reject(id));
  }

  protected switchTo(householdId: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.switchActive(householdId).subscribe({
      next: () => {
        this.store.load();
        this.loading.set(false);
        this.router.navigateByUrl('/dashboard');
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Operacja nie powiodła się, spróbuj ponownie');
      },
    });
  }

  protected askLeave(householdId: string): void {
    this.pendingLeaveId.set(householdId);
  }

  protected cancelLeave(): void {
    this.pendingLeaveId.set(null);
  }

  protected leave(householdId: string): void {
    this.pendingLeaveId.set(null);
    this.run(() => this.api.leave(householdId));
  }

  protected cancelInvitation(): void {
    this.reject();
  }

  protected setEmail(value: string): void {
    this.inviteEmail.set(value);
  }

  private run(action: () => Observable<void>): void {
    this.loading.set(true);
    this.error.set(null);
    action().subscribe({
      next: () => {
        this.store.load();
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Operacja nie powiodła się, spróbuj ponownie');
      },
    });
  }
}
