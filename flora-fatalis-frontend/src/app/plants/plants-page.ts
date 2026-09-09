import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';

import { LocationsApiService } from '../locations/locations-api.service';
import { LocationDto } from '../locations/location.dto';
import { SpeciesApiService } from '../species/species-api.service';
import { SpeciesDto } from '../species/species.dto';
import { PlantDto } from './plant.dto';
import { PlantsApiService } from './plants-api.service';

@Component({
  selector: 'app-plants-page',
  imports: [RouterLink],
  templateUrl: './plants-page.html',
  styleUrl: './plants-page.css',
})
export class PlantsPage {
  private readonly plantsApi = inject(PlantsApiService);
  private readonly speciesApi = inject(SpeciesApiService);
  private readonly locationsApi = inject(LocationsApiService);

  protected readonly plants = signal<PlantDto[]>([]);
  protected readonly species = signal<SpeciesDto[]>([]);
  protected readonly locations = signal<LocationDto[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    forkJoin({
      plants: this.plantsApi.list(),
      species: this.speciesApi.list(),
      locations: this.locationsApi.list(),
    }).subscribe({
      next: ({ plants, species, locations }) => {
        this.plants.set(plants);
        this.species.set(species);
        this.locations.set(locations);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(
          err.status === 409
            ? 'Najpierw połącz gospodarstwo w Domownikach'
            : 'Nie udało się pobrać roślin',
        );
      },
    });
  }

  protected speciesName(id: string): string {
    return this.species().find((item) => item.id === id)?.name ?? 'Nieznany gatunek';
  }

  protected speciesLatin(id: string): string | null {
    return this.species().find((item) => item.id === id)?.latinName ?? null;
  }

  protected locationName(id: string): string {
    return this.locations().find((item) => item.id === id)?.name ?? 'Nieznana lokalizacja';
  }
}
