import { Component, inject, signal } from '@angular/core';

import { SpeciesApiService } from './species-api.service';
import { SpeciesDto } from './species.dto';

@Component({
  selector: 'app-species-page',
  templateUrl: './species-page.html',
  styleUrl: './species-page.css',
})
export class SpeciesPage {
  private readonly api = inject(SpeciesApiService);

  protected readonly species = signal<SpeciesDto[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.api.list().subscribe({
      next: (items) => {
        this.species.set(items);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Nie udało się pobrać gatunków');
        this.loading.set(false);
      },
    });
  }

  protected categoryLabel(category: string): string {
    if (category === 'domowa') return 'Domowa';
    if (category === 'zioła') return 'Zioła';
    if (category === 'balkonowe') return 'Balkonowa';
    if (category === 'inne') return 'Inne';
    return category;
  }
}
