import { AsyncPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { forkJoin } from 'rxjs';

import { CareApiService } from '../care/care-api.service';
import { CareDashboardDto, DashboardItemDto } from '../care/care.dto';
import { LocationsApiService } from '../locations/locations-api.service';
import { PhotoUrlService } from '../photos/photo-url.service';
import { PlantDto } from '../plants/plant.dto';
import { PlantsApiService } from '../plants/plants-api.service';

@Component({
  selector: 'app-dashboard-page',
  imports: [AsyncPipe, RouterLink, FormsModule],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.css',
})
export class DashboardPage {
  private readonly api = inject(CareApiService);
  private readonly plantsApi = inject(PlantsApiService);
  private readonly locationsApi = inject(LocationsApiService);
  protected readonly photoUrls = inject(PhotoUrlService);

  protected readonly dashboard = signal<CareDashboardDto | null>(null);
  protected readonly plants = signal<PlantDto[]>([]);
  protected readonly locationNames = signal<Record<string, string>>({});
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly wateringPlantId = signal<string | null>(null);
  protected readonly quantityDrafts = signal<Record<string, string>>({});
  protected readonly hasAnyTasks = computed(() => {
    const data = this.dashboard();
    if (!data) return false;
    return data.overdue.length + data.dueToday.length + data.upcoming.length > 0;
  });
  protected readonly todayItems = computed(() => {
    const data = this.dashboard();
    if (!data) return [];
    return [...data.overdue, ...data.dueToday];
  });
  protected readonly collection = computed(() => this.plants().slice(0, 8));

  constructor() {
    this.reload();
  }

  protected overdueLabel(days: number): string {
    if (days === 1) return '1 dzień zaległości';
    return `${days} dni zaległości`;
  }

  protected todayLabel(): string {
    return new Intl.DateTimeFormat('pl-PL', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      timeZone: 'Europe/Warsaw',
    }).format(new Date());
  }

  protected careLabel(type: string): string {
    return type === 'FERTILIZING' ? 'nawożenie' : 'podlewanie';
  }

  protected actionVerb(type: string): string {
    return type === 'FERTILIZING' ? 'Nawieź' : 'Podlej';
  }

  protected itemKey(item: DashboardItemDto): string {
    return item.plantId + item.careType;
  }

  protected dueDate(iso: string): string {
    return new Intl.DateTimeFormat('pl-PL', {
      day: 'numeric',
      month: 'long',
      timeZone: 'Europe/Warsaw',
    }).format(new Date(`${iso}T12:00:00`));
  }

  protected water(item: DashboardItemDto): void {
    const quantityMl = this.parseMl(this.quantityDrafts()[item.plantId]);
    if (quantityMl === 'invalid') {
      this.error.set('Ilość wody musi być liczbą całkowitą większą od 0');
      return;
    }
    this.wateringPlantId.set(this.itemKey(item));
    this.api.water(item.plantId, quantityMl).subscribe({
      next: () => {
        this.wateringPlantId.set(null);
        this.quantityDrafts.update((drafts) => ({ ...drafts, [item.plantId]: '' }));
        this.reload();
      },
      error: () => {
        this.wateringPlantId.set(null);
        this.error.set('Nie udało się oznaczyć podlewania');
      },
    });
  }

  protected fertilize(item: DashboardItemDto): void {
    this.wateringPlantId.set(this.itemKey(item));
    this.api.fertilize(item.plantId).subscribe({
      next: () => {
        this.wateringPlantId.set(null);
        this.reload();
      },
      error: () => {
        this.wateringPlantId.set(null);
        this.error.set('Nie udało się oznaczyć nawożenia');
      },
    });
  }

  protected locationFor(item: DashboardItemDto): string {
    return item.locationName || '';
  }

  protected plantLocation(plant: PlantDto): string {
    return this.locationNames()[plant.locationId] ?? '';
  }

  protected quantityFor(plantId: string): string {
    return this.quantityDrafts()[plantId] || '';
  }

  protected setQuantity(plantId: string, value: string | number | null): void {
    this.quantityDrafts.update((drafts) => ({
      ...drafts,
      [plantId]: value == null ? '' : String(value),
    }));
  }

  private parseMl(raw: string | number | null | undefined): number | null | 'invalid' {
    if (raw == null || raw === '') {
      return null;
    }
    const quantityMl = typeof raw === 'number' ? raw : Number(String(raw).trim());
    if (!Number.isInteger(quantityMl) || quantityMl < 1) {
      return 'invalid';
    }
    return quantityMl;
  }

  private reload(): void {
    forkJoin({
      dashboard: this.api.dashboard(),
      plants: this.plantsApi.list(),
      locations: this.locationsApi.list(),
    }).subscribe({
      next: ({ dashboard, plants, locations }) => {
        this.dashboard.set(dashboard);
        this.plants.set(plants);
        this.locationNames.set(
          Object.fromEntries(locations.map((location) => [location.id, location.name])),
        );
        this.loading.set(false);
        this.error.set(null);
      },
      error: () => {
        this.error.set('Nie udało się pobrać dashboardu');
        this.loading.set(false);
      },
    });
  }
}
