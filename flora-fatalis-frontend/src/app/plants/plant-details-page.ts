import { AsyncPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CareApiService } from '../care/care-api.service';
import { LocationsApiService } from '../locations/locations-api.service';
import { compressImage } from '../photos/compress-image';
import { PlantPhotoDto } from '../photos/photo.dto';
import { PhotoUrlService } from '../photos/photo-url.service';
import { PhotosApiService } from '../photos/photos-api.service';
import { SpeciesApiService } from '../species/species-api.service';
import { SpeciesDto } from '../species/species.dto';
import { PlantHistoryDayGroup, PlantHistoryItemDto } from './plant-history.dto';
import { PlantDto } from './plant.dto';
import { PlantsApiService } from './plants-api.service';

@Component({
  selector: 'app-plant-details-page',
  imports: [RouterLink, AsyncPipe, FormsModule],
  templateUrl: './plant-details-page.html',
  styleUrl: './plant-details-page.css',
})
export class PlantDetailsPage {
  private readonly plantsApi = inject(PlantsApiService);
  private readonly speciesApi = inject(SpeciesApiService);
  private readonly locationsApi = inject(LocationsApiService);
  private readonly photosApi = inject(PhotosApiService);
  protected readonly photoUrls = inject(PhotoUrlService);
  private readonly careApi = inject(CareApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly plantId = this.route.snapshot.paramMap.get('id') ?? '';

  protected readonly plant = signal<PlantDto | null>(null);
  protected readonly species = signal<SpeciesDto | null>(null);
  protected readonly locationName = signal('');
  protected readonly photos = signal<PlantPhotoDto[]>([]);
  protected readonly history = signal<PlantHistoryItemDto[]>([]);
  protected readonly historyDays = computed(() => this.groupHistory(this.history()));
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly archiving = signal(false);
  protected readonly watering = signal(false);
  protected readonly uploading = signal(false);
  protected readonly deletingEventId = signal<string | null>(null);
  protected readonly quantityMl = signal('');

  protected readonly speciesIntervalDays = computed(
    () => this.species()?.defaultWateringIntervalDays ?? null,
  );

  constructor() {
    this.reload();
  }

  protected archive(): void {
    this.archiving.set(true);
    this.plantsApi.archive(this.plantId).subscribe({
      next: () => this.router.navigateByUrl('/plants'),
      error: () => {
        this.archiving.set(false);
        this.error.set('Nie udało się zarchiwizować rośliny');
      },
    });
  }

  protected setQuantityMl(value: string | number | null): void {
    this.quantityMl.set(value == null ? '' : String(value));
  }

  protected water(): void {
    const quantityMl = this.parseMl(this.quantityMl());
    if (quantityMl === 'invalid') {
      this.error.set('Ilość wody musi być liczbą całkowitą większą od 0');
      return;
    }
    this.watering.set(true);
    this.careApi.water(this.plantId, quantityMl).subscribe({
      next: () => {
        this.watering.set(false);
        this.quantityMl.set('');
        this.reloadHistory();
      },
      error: () => {
        this.watering.set(false);
        this.error.set('Nie udało się oznaczyć podlewania');
      },
    });
  }

  protected deleteEvent(event: PlantHistoryItemDto): void {
    this.deletingEventId.set(event.sourceId);
    this.careApi.deleteEvent(event.sourceId).subscribe({
      next: () => {
        this.deletingEventId.set(null);
        this.reloadHistory();
      },
      error: () => {
        this.deletingEventId.set(null);
        this.error.set('Nie udało się usunąć podlewania');
      },
    });
  }

  protected async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.uploading.set(true);
    try {
      const compressed = await compressImage(file);
      this.photosApi.upload(this.plantId, compressed).subscribe({
        next: () => {
          this.uploading.set(false);
          this.reloadPhotos();
        },
        error: () => {
          this.uploading.set(false);
          this.error.set('Nie udało się dodać zdjęcia');
        },
      });
    } catch {
      this.uploading.set(false);
      this.error.set('Nie udało się przetworzyć zdjęcia');
    }
    input.value = '';
  }

  protected setPrimary(photo: PlantPhotoDto): void {
    this.photosApi.setPrimary(photo.id).subscribe({
      next: () => this.reloadPhotos(),
      error: () => this.error.set('Nie udało się ustawić zdjęcia głównego'),
    });
  }

  protected deletePhoto(photo: PlantPhotoDto): void {
    this.photosApi.delete(photo.id).subscribe({
      next: () => this.reloadPhotos(),
      error: () => this.error.set('Nie udało się usunąć zdjęcia'),
    });
  }

  private reload(): void {
    forkJoin({
      plant: this.plantsApi.get(this.plantId),
      species: this.speciesApi.list(),
      locations: this.locationsApi.list(),
      photos: this.photosApi.list(this.plantId),
      history: this.plantsApi.history(this.plantId),
    }).subscribe({
      next: ({ plant, species, locations, photos, history }) => {
        this.plant.set(plant);
        this.species.set(species.find((item) => item.id === plant.speciesId) ?? null);
        this.locationName.set(locations.find((item) => item.id === plant.locationId)?.name ?? '');
        this.photos.set(photos);
        this.history.set(history);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Nie znaleziono rośliny');
        this.loading.set(false);
      },
    });
  }

  private reloadPhotos(): void {
    this.photosApi.list(this.plantId).subscribe((photos) => this.photos.set(photos));
    this.reloadHistory();
  }

  private reloadHistory(): void {
    this.plantsApi.history(this.plantId).subscribe((history) => this.history.set(history));
  }

  protected eventTime(iso: string): string {
    return new Intl.DateTimeFormat('pl-PL', {
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'Europe/Warsaw',
    }).format(new Date(iso));
  }

  private groupHistory(items: PlantHistoryItemDto[]): PlantHistoryDayGroup[] {
    const groups = new Map<string, PlantHistoryDayGroup>();
    for (const item of items) {
      const key = this.dayKey(item.occurredAt);
      const existing = groups.get(key);
      if (existing) {
        existing.items.push(item);
      } else {
        groups.set(key, { key, label: this.dayLabel(key), items: [item] });
      }
    }
    return [...groups.values()];
  }

  private dayKey(iso: string): string {
    const parts = new Intl.DateTimeFormat('en-CA', {
      timeZone: 'Europe/Warsaw',
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).formatToParts(new Date(iso));
    const year = parts.find((part) => part.type === 'year')?.value;
    const month = parts.find((part) => part.type === 'month')?.value;
    const day = parts.find((part) => part.type === 'day')?.value;
    return `${year}-${month}-${day}`;
  }

  private dayLabel(key: string): string {
    const today = this.dayKey(new Date().toISOString());
    const yesterdayDate = new Date();
    yesterdayDate.setDate(yesterdayDate.getDate() - 1);
    const yesterday = this.dayKey(yesterdayDate.toISOString());
    if (key === today) return 'Dzisiaj';
    if (key === yesterday) return 'Wczoraj';
    const [year, month, day] = key.split('-').map(Number);
    return new Intl.DateTimeFormat('pl-PL', {
      day: 'numeric',
      month: 'long',
    }).format(new Date(year, month - 1, day));
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
}
