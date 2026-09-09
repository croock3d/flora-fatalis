import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { LocationDto, LocationKind } from './location.dto';
import { LocationsApiService } from './locations-api.service';

@Component({
  selector: 'app-locations-page',
  imports: [FormsModule],
  templateUrl: './locations-page.html',
  styleUrl: './locations-page.css',
})
export class LocationsPage {
  private readonly api = inject(LocationsApiService);

  protected readonly locations = signal<LocationDto[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly name = signal('');
  protected readonly kind = signal<LocationKind>('INDOOR');
  protected readonly editingId = signal<string | null>(null);

  constructor() {
    this.reload();
  }

  protected kindLabel(kind: LocationKind): string {
    if (kind === 'INDOOR') return 'W domu';
    if (kind === 'BALCONY') return 'Balkon';
    return 'Na zewnątrz';
  }

  protected startEdit(location: LocationDto): void {
    this.editingId.set(location.id);
    this.name.set(location.name);
    this.kind.set(location.kind);
  }

  protected cancelEdit(): void {
    this.editingId.set(null);
    this.name.set('');
    this.kind.set('INDOOR');
  }

  protected save(): void {
    const name = this.name().trim();
    if (!name) return;

    this.saving.set(true);
    this.error.set(null);
    const request = { name, kind: this.kind() };
    const editingId = this.editingId();
    const request$ = editingId
      ? this.api.update(editingId, request)
      : this.api.create(request);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelEdit();
        this.reload();
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        if (err.status === 409) {
          this.error.set('Najpierw połącz gospodarstwo w Domownikach');
        } else {
          this.error.set('Nie udało się zapisać lokalizacji');
        }
      },
    });
  }

  protected remove(location: LocationDto): void {
    this.saving.set(true);
    this.error.set(null);
    this.api.delete(location.id).subscribe({
      next: () => {
        this.saving.set(false);
        this.reload();
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        if (err.status === 409) {
          this.error.set('Nie można usunąć lokalizacji, bo są do niej przypisane rośliny');
        } else {
          this.error.set('Nie udało się usunąć lokalizacji');
        }
      },
    });
  }

  private reload(): void {
    this.loading.set(true);
    this.api.list().subscribe({
      next: (items) => {
        this.locations.set(items);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        if (err.status === 409) {
          this.error.set('Najpierw połącz gospodarstwo w Domownikach');
          this.locations.set([]);
        } else {
          this.error.set('Nie udało się pobrać lokalizacji');
        }
      },
    });
  }
}
