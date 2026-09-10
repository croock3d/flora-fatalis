import { Injectable, computed, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';

import { HouseholdApiService } from './household-api.service';
import { HouseholdStatusResponse } from './household.dto';

@Injectable({ providedIn: 'root' })
export class HouseholdStore {
  private readonly api = inject(HouseholdApiService);

  private readonly _status = signal<HouseholdStatusResponse | null>(null);
  private readonly _loaded = signal(false);

  readonly status = computed(() => this._status());
  readonly loaded = computed(() => this._loaded());
  readonly hasPendingInvitation = computed(() => !!this._status()?.incomingInvitation);

  load(): void {
    this.api
      .getStatus()
      .pipe(
        catchError(() =>
          of({
            activeHouseholdId: null,
            households: [],
            incomingInvitation: null,
            outgoingInvitation: null,
          }),
        ),
      )
      .subscribe((status) => {
        this._status.set(status);
        this._loaded.set(true);
      });
  }

  clear(): void {
    this._status.set(null);
    this._loaded.set(false);
  }
}
