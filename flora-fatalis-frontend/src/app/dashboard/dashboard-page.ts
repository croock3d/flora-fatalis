import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { CareApiService } from '../care/care-api.service';
import { CareDashboardDto, DashboardItemDto } from '../care/care.dto';

@Component({
  selector: 'app-dashboard-page',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './dashboard-page.html',
  styleUrl: './dashboard-page.css',
})
export class DashboardPage {
  private readonly api = inject(CareApiService);

  protected readonly dashboard = signal<CareDashboardDto | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly wateringPlantId = signal<string | null>(null);
  protected readonly quantityDrafts = signal<Record<string, string>>({});

  constructor() {
    this.reload();
  }

  protected overdueLabel(days: number): string {
    if (days === 1) return '1 dzień zaległości';
    return `${days} dni zaległości`;
  }

  protected careLabel(type: string): string {
    return type === 'FERTILIZING' ? 'nawożenie' : 'podlewanie';
  }

  protected careIcon(type: string): string {
    return type === 'FERTILIZING' ? '🌿' : '💧';
  }

  protected itemKey(item: DashboardItemDto): string {
    return item.plantId + item.careType;
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
    this.api.dashboard().subscribe({
      next: (dashboard) => {
        this.dashboard.set(dashboard);
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
