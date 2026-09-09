import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { CareApiService } from '../care/care-api.service';
import { LocationsApiService } from '../locations/locations-api.service';
import { PhotosApiService } from '../photos/photos-api.service';
import { SpeciesApiService } from '../species/species-api.service';
import { PlantDetailsPage } from './plant-details-page';
import { PlantsApiService } from './plants-api.service';

describe('PlantDetailsPage', () => {
  const plant = {
    id: 'plant-1',
    speciesId: 'species-1',
    locationId: 'location-1',
    name: 'Monstera',
    wateringIntervalDaysOverride: null,
    fertilizingIntervalDaysOverride: null,
    acquiredAt: null,
    archivedAt: null,
    createdAt: '2026-01-01T08:00:00Z',
  };

  let careApi: {
    careStatus: ReturnType<typeof vi.fn>;
    prune: ReturnType<typeof vi.fn>;
    water: ReturnType<typeof vi.fn>;
    fertilize: ReturnType<typeof vi.fn>;
    deleteEvent: ReturnType<typeof vi.fn>;
  };
  let plantsApi: { get: ReturnType<typeof vi.fn>; history: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    plantsApi = {
      get: vi.fn().mockReturnValue(of(plant)),
      history: vi.fn().mockReturnValue(of([])),
    };
    careApi = {
      careStatus: vi.fn().mockReturnValue(
        of({
          watering: { lastAt: null, nextOn: '2026-01-12', intervalDays: 7 },
          fertilizing: null,
        }),
      ),
      prune: vi.fn().mockReturnValue(of({})),
      water: vi.fn(),
      fertilize: vi.fn(),
      deleteEvent: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [PlantDetailsPage],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: PlantsApiService, useValue: plantsApi },
        { provide: CareApiService, useValue: careApi },
        { provide: SpeciesApiService, useValue: { list: () => of([]) } },
        { provide: LocationsApiService, useValue: { list: () => of([]) } },
        { provide: PhotosApiService, useValue: { list: () => of([]) } },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: { get: () => 'plant-1' } } },
        },
      ],
    }).compileComponents();
  });

  it('saves a pruning event from the form', async () => {
    const fixture = TestBed.createComponent(PlantDetailsPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const openButton = [...compiled.querySelectorAll('button')].find((button) =>
      button.textContent?.includes('Przycięto'),
    );
    openButton?.click();
    fixture.detectChanges();

    const page = fixture.componentInstance as unknown as {
      setPrunePerformedOn(value: string): void;
      setPruneKind(value: string): void;
      setPruneNotes(value: string): void;
    };
    page.setPrunePerformedOn('2026-01-03');
    page.setPruneKind('DRY_LEAVES');
    page.setPruneNotes('suche liście');
    fixture.detectChanges();

    compiled.querySelector('form')?.dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    await fixture.whenStable();

    expect(careApi.prune).toHaveBeenCalledWith('plant-1', {
      performedOn: '2026-01-03',
      pruningKind: 'DRY_LEAVES',
      notes: 'suche liście',
    });
  });

  it('renders pruning events on the timeline', async () => {
    plantsApi.history.mockReturnValue(
      of([
        {
          type: 'PRUNING',
          occurredAt: '2026-01-10T08:00:00Z',
          sourceId: 'event-1',
          quantityMl: null,
          performedBy: 'user-1',
          performedByName: 'Dev',
          photoUrl: null,
          notes: 'formowanie korony',
          pruningKind: 'SHAPING',
        },
      ]),
    );

    const fixture = TestBed.createComponent(PlantDetailsPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Przycinanie');
    expect(compiled.textContent).toContain('Formowanie');
    expect(compiled.textContent).toContain('formowanie korony');
  });

  it('reloads care status after deleting a watering event', async () => {
    plantsApi.history.mockReturnValue(
      of([
        {
          type: 'WATERING',
          occurredAt: '2026-01-10T08:00:00Z',
          sourceId: 'event-1',
          quantityMl: 250,
          performedBy: 'user-1',
          performedByName: 'Dev',
          photoUrl: null,
          notes: null,
          pruningKind: null,
        },
      ]),
    );

    const fixture = TestBed.createComponent(PlantDetailsPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const deleteButton = [...(fixture.nativeElement as HTMLElement).querySelectorAll('button')].find(
      (button) => button.textContent?.trim() === 'Usuń',
    );
    deleteButton?.click();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(careApi.deleteEvent).toHaveBeenCalledWith('event-1');
    expect(plantsApi.history.mock.calls.length).toBeGreaterThanOrEqual(2);
    expect(careApi.careStatus.mock.calls.length).toBeGreaterThanOrEqual(2);
  });
});
