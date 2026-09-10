import { AsyncPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { CareApiService } from '../care/care-api.service';
import { PlantCareStatusDto, PruningKind } from '../care/care.dto';
import { parseQuantityMl } from '../care/parse-quantity-ml';
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
  protected readonly careStatus = signal<PlantCareStatusDto | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly archiving = signal(false);
  protected readonly watering = signal(false);
  protected readonly fertilizing = signal(false);
  protected readonly pruning = signal(false);
  protected readonly showPruneForm = signal(false);
  protected readonly uploading = signal(false);
  protected readonly deletingEventId = signal<string | null>(null);
  protected readonly quantityMl = signal('');
  protected readonly prunePerformedOn = signal(this.todayInWarsaw());
  protected readonly pruneKind = signal<PruningKind | ''>('');
  protected readonly pruneNotes = signal('');
  protected readonly confirmingArchive = signal(false);
  protected readonly pendingDeleteId = signal<string | null>(null);
  protected readonly primaryPhoto = computed(
    () => this.photos().find((photo) => photo.primary) ?? this.photos()[0] ?? null,
  );
  protected readonly pruneKinds: { value: PruningKind; label: string }[] = [
    { value: 'DRY_LEAVES', label: 'Suche/uszkodzone liście' },
    { value: 'SHAPING', label: 'Formowanie' },
    { value: 'HEAVY_PRUNING', label: 'Mocniejsze cięcie' },
    { value: 'OTHER', label: 'Inne' },
  ];

  constructor() {
    this.reload();
  }

  protected askArchive(): void {
    this.confirmingArchive.set(true);
  }

  protected cancelArchive(): void {
    this.confirmingArchive.set(false);
  }

  protected archive(): void {
    this.error.set(null);
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
    const quantityMl = parseQuantityMl(this.quantityMl());
    if (quantityMl === 'invalid') {
      this.error.set('Ilość wody musi być liczbą całkowitą większą od 0');
      return;
    }
    this.error.set(null);
    this.watering.set(true);
    this.careApi.water(this.plantId, quantityMl).subscribe({
      next: () => {
        this.watering.set(false);
        this.quantityMl.set('');
        this.reloadHistory();
        this.reloadCareStatus();
      },
      error: () => {
        this.watering.set(false);
        this.error.set('Nie udało się oznaczyć podlewania');
      },
    });
  }

  protected fertilize(): void {
    this.error.set(null);
    this.fertilizing.set(true);
    this.careApi.fertilize(this.plantId).subscribe({
      next: () => {
        this.fertilizing.set(false);
        this.reloadHistory();
        this.reloadCareStatus();
      },
      error: () => {
        this.fertilizing.set(false);
        this.error.set('Nie udało się oznaczyć nawożenia');
      },
    });
  }

  protected openPruneForm(): void {
    this.showPruneForm.set(true);
    this.prunePerformedOn.set(this.todayInWarsaw());
    this.pruneKind.set('');
    this.pruneNotes.set('');
  }

  protected cancelPruneForm(): void {
    this.showPruneForm.set(false);
  }

  protected setPrunePerformedOn(value: string): void {
    this.prunePerformedOn.set(value);
  }

  protected setPruneKind(value: string): void {
    this.pruneKind.set(value as PruningKind | '');
  }

  protected setPruneNotes(value: string): void {
    this.pruneNotes.set(value);
  }

  protected prune(): void {
    const performedOn = this.prunePerformedOn().trim();
    if (!performedOn) {
      this.error.set('Wybierz datę przycięcia');
      return;
    }
    this.error.set(null);
    this.pruning.set(true);
    this.careApi
      .prune(this.plantId, {
        performedOn,
        pruningKind: this.pruneKind() || null,
        notes: this.pruneNotes().trim() || null,
      })
      .subscribe({
        next: () => {
          this.pruning.set(false);
          this.showPruneForm.set(false);
          this.reloadHistory();
        },
        error: () => {
          this.pruning.set(false);
          this.error.set('Nie udało się oznaczyć przycinania');
        },
      });
  }

  protected pruningKindLabel(kind: PruningKind | null | undefined): string {
    return this.pruneKinds.find((item) => item.value === kind)?.label ?? '';
  }

  protected askDeleteEvent(event: PlantHistoryItemDto): void {
    this.pendingDeleteId.set(event.sourceId);
  }

  protected cancelDeleteEvent(): void {
    this.pendingDeleteId.set(null);
  }

  protected deleteEvent(event: PlantHistoryItemDto): void {
    this.error.set(null);
    this.deletingEventId.set(event.sourceId);
    this.pendingDeleteId.set(null);
    this.careApi.deleteEvent(event.sourceId).subscribe({
      next: () => {
        this.deletingEventId.set(null);
        this.reloadHistory();
        this.reloadCareStatus();
      },
      error: () => {
        this.deletingEventId.set(null);
        this.error.set('Nie udało się usunąć wpisu');
      },
    });
  }

  protected async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.error.set(null);
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
    this.error.set(null);
    this.photosApi.setPrimary(photo.id).subscribe({
      next: () => this.reloadPhotos(),
      error: () => this.error.set('Nie udało się ustawić zdjęcia głównego'),
    });
  }

  protected deletePhoto(photo: PlantPhotoDto): void {
    this.error.set(null);
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
      careStatus: this.careApi.careStatus(this.plantId),
    }).subscribe({
      next: ({ plant, species, locations, photos, history, careStatus }) => {
        this.plant.set(plant);
        this.species.set(species.find((item) => item.id === plant.speciesId) ?? null);
        this.locationName.set(locations.find((item) => item.id === plant.locationId)?.name ?? '');
        this.photos.set(photos);
        this.history.set(history);
        this.careStatus.set(careStatus);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Nie znaleziono rośliny');
        this.loading.set(false);
      },
    });
  }

  private reloadPhotos(): void {
    this.photosApi.list(this.plantId).subscribe({
      next: (photos) => this.photos.set(photos),
      error: () => this.error.set('Nie udało się odświeżyć zdjęć'),
    });
    this.reloadHistory();
  }

  private reloadHistory(): void {
    this.plantsApi.history(this.plantId).subscribe({
      next: (history) => this.history.set(history),
      error: () => this.error.set('Nie udało się odświeżyć historii'),
    });
  }

  private reloadCareStatus(): void {
    this.careApi.careStatus(this.plantId).subscribe({
      next: (status) => this.careStatus.set(status),
      error: () => this.error.set('Nie udało się odświeżyć statusu pielęgnacji'),
    });
  }

  protected careDate(iso: string | null | undefined): string {
    if (!iso) return 'brak';
    const date = /^\d{4}-\d{2}-\d{2}$/.test(iso) ? `${iso}T12:00:00` : iso;
    return new Intl.DateTimeFormat('pl-PL', {
      day: 'numeric',
      month: 'long',
      timeZone: 'Europe/Warsaw',
    }).format(new Date(date));
  }

  protected daysFromToday(iso: string | null | undefined): number | null {
    if (!iso) return null;
    const today = this.todayInWarsaw();
    const target = /^\d{4}-\d{2}-\d{2}$/.test(iso) ? iso : this.dayKey(iso);
    const todayMs = Date.parse(`${today}T12:00:00`);
    const targetMs = Date.parse(`${target}T12:00:00`);
    return Math.round((targetMs - todayMs) / 86_400_000);
  }

  protected overdueLabel(days: number): string {
    const overdue = Math.abs(days);
    if (overdue === 1) return '1 dzień zaległości';
    return `${overdue} dni zaległości`;
  }

  protected wateringDue(): boolean {
    const days = this.daysFromToday(this.careStatus()?.watering.nextOn);
    return days != null && days <= 0;
  }

  protected statusTone(): 'overdue' | 'today' | 'ok' {
    const watering = this.daysFromToday(this.careStatus()?.watering.nextOn);
    const fertilizing = this.daysFromToday(this.careStatus()?.fertilizing?.nextOn);
    if ((watering != null && watering < 0) || (fertilizing != null && fertilizing < 0)) {
      return 'overdue';
    }
    if (watering === 0 || fertilizing === 0) {
      return 'today';
    }
    return 'ok';
  }

  protected statusLine(): string {
    const watering = this.daysFromToday(this.careStatus()?.watering.nextOn);
    const fertilizing = this.daysFromToday(this.careStatus()?.fertilizing?.nextOn);
    if (watering != null && watering < 0) {
      return `Podlej · ${this.overdueLabel(watering)}`;
    }
    if (watering === 0) {
      return 'Podlej · dzisiaj';
    }
    if (fertilizing != null && fertilizing < 0) {
      return `Nawieź · ${this.overdueLabel(fertilizing)}`;
    }
    if (fertilizing === 0) {
      return 'Nawieź · dzisiaj';
    }
    if (this.careStatus()?.watering.nextOn) {
      return `Następne podlewanie · ${this.careDate(this.careStatus()?.watering.nextOn)}`;
    }
    return 'Brak zaplanowanego podlewania';
  }

  protected eventTitle(type: PlantHistoryItemDto['type']): string {
    switch (type) {
      case 'WATERING':
        return 'Podlano';
      case 'FERTILIZING':
        return 'Nawożono';
      case 'PRUNING':
        return 'Przycinanie';
      case 'PHOTO':
        return 'Zdjęcie';
      default:
        return 'Dodano roślinę';
    }
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

  protected todayInWarsaw(): string {
    return this.dayKey(new Date().toISOString());
  }
}
