import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { CareApiService } from './care-api.service';

describe('CareApiService', () => {
  let service: CareApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CareApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('loads the care dashboard', () => {
    service.dashboard().subscribe();

    const req = http.expectOne('/api/dashboard');
    expect(req.request.method).toBe('GET');
    req.flush({ overdue: [], dueToday: [], upcoming: [] });
  });

  it('posts a watering event', () => {
    service.water('plant-1', 250).subscribe();

    const req = http.expectOne('/api/plants/plant-1/water');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ quantityMl: 250 });
    req.flush({
      id: 'event-1',
      plantId: 'plant-1',
      careType: 'WATERING',
      performedAt: '2026-01-10T08:00:00Z',
      performedBy: 'user-1',
      performedByName: 'Dev',
      quantityMl: 250,
      notes: null,
      pruningKind: null,
    });
  });

  it('posts a fertilizing event', () => {
    service.fertilize('plant-1').subscribe();

    const req = http.expectOne('/api/plants/plant-1/fertilize');
    expect(req.request.method).toBe('POST');
    req.flush({
      id: 'event-2',
      plantId: 'plant-1',
      careType: 'FERTILIZING',
      performedAt: '2026-01-10T08:00:00Z',
      performedBy: 'user-1',
      performedByName: 'Dev',
      quantityMl: null,
      notes: null,
      pruningKind: null,
    });
  });

  it('posts a pruning event', () => {
    service
      .prune('plant-1', {
        performedOn: '2026-01-10',
        pruningKind: 'SHAPING',
        notes: 'formowanie',
      })
      .subscribe();

    const req = http.expectOne('/api/plants/plant-1/prune');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      performedOn: '2026-01-10',
      pruningKind: 'SHAPING',
      notes: 'formowanie',
    });
    req.flush({
      id: 'event-1',
      plantId: 'plant-1',
      careType: 'PRUNING',
      performedAt: '2026-01-10T08:00:00Z',
      performedBy: 'user-1',
      performedByName: 'Dev',
      quantityMl: null,
      notes: 'formowanie',
      pruningKind: 'SHAPING',
    });
  });
});
