import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';

import { LocationsApiService } from '../locations/locations-api.service';
import { LocationDto } from '../locations/location.dto';
import { SpeciesApiService } from '../species/species-api.service';
import { SpeciesDto } from '../species/species.dto';
import { PlantRequest } from './plant.dto';
import { PlantsApiService } from './plants-api.service';

@Component({
  selector: 'app-plant-form-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './plant-form-page.html',
  styleUrl: './plant-form-page.css',
})
export class PlantFormPage {
  private readonly fb = inject(FormBuilder);
  private readonly plantsApi = inject(PlantsApiService);
  private readonly speciesApi = inject(SpeciesApiService);
  private readonly locationsApi = inject(LocationsApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly plantId = this.route.snapshot.paramMap.get('id');
  protected readonly isEdit = !!this.plantId;
  protected readonly species = signal<SpeciesDto[]>([]);
  protected readonly locations = signal<LocationDto[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(1)]],
    speciesId: ['', Validators.required],
    locationId: ['', Validators.required],
    wateringIntervalDaysOverride: [''],
    fertilizingIntervalDaysOverride: [''],
  });

  protected selectedSpecies(): SpeciesDto | undefined {
    return this.species().find((item) => item.id === this.form.controls.speciesId.value);
  }

  protected selectedSpeciesInterval(): number | null {
    return this.selectedSpecies()?.defaultWateringIntervalDays ?? null;
  }

  protected selectedFertilizingInterval(): number | null {
    return this.selectedSpecies()?.defaultFertilizingIntervalDays ?? null;
  }

  protected clearOverride(): void {
    this.form.controls.wateringIntervalDaysOverride.setValue('');
  }

  protected clearFertilizingOverride(): void {
    this.form.controls.fertilizingIntervalDaysOverride.setValue('');
  }

  constructor() {
    forkJoin({
      species: this.speciesApi.list(),
      locations: this.locationsApi.list(),
    }).subscribe({
      next: ({ species, locations }) => {
        this.species.set(species);
        this.locations.set(locations);
        if (this.plantId) {
          this.plantsApi.get(this.plantId).subscribe({
            next: (plant) => {
              this.form.patchValue({
                name: plant.name,
                speciesId: plant.speciesId,
                locationId: plant.locationId,
                wateringIntervalDaysOverride:
                  plant.wateringIntervalDaysOverride?.toString() ?? '',
                fertilizingIntervalDaysOverride:
                  plant.fertilizingIntervalDaysOverride?.toString() ?? '',
              });
              this.loading.set(false);
            },
            error: () => {
              this.error.set('Nie znaleziono rośliny');
              this.loading.set(false);
            },
          });
        } else {
          this.loading.set(false);
        }
      },
      error: () => {
        this.error.set('Nie udało się pobrać danych formularza');
        this.loading.set(false);
      },
    });
  }

  protected save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const override = value.wateringIntervalDaysOverride.trim();
    const fertilizingOverride = value.fertilizingIntervalDaysOverride.trim();
    const request: PlantRequest = {
      name: value.name.trim(),
      speciesId: value.speciesId,
      locationId: value.locationId,
      wateringIntervalDaysOverride: override ? Number(override) : null,
      fertilizingIntervalDaysOverride: fertilizingOverride ? Number(fertilizingOverride) : null,
      acquiredAt: null,
    };

    this.saving.set(true);
    this.error.set(null);
    const request$ = this.plantId
      ? this.plantsApi.update(this.plantId, request)
      : this.plantsApi.create(request);

    request$.subscribe({
      next: (plant) => this.router.navigateByUrl(`/plants/${plant.id}`),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        if (err.status === 409) {
          this.error.set('Najpierw połącz gospodarstwo w Domownikach');
        } else {
          this.error.set('Nie udało się zapisać rośliny');
        }
      },
    });
  }
}
