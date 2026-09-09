import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { LocationsApiService } from './locations-api.service';
import { LocationsPage } from './locations-page';

describe('LocationsPage', () => {
  let api: {
    list: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };

  const salon = { id: 'loc-1', name: 'Salon', kind: 'INDOOR' as const };

  beforeEach(async () => {
    api = {
      list: vi.fn().mockReturnValue(of([salon])),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn().mockReturnValue(of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [LocationsPage],
      providers: [provideHttpClient(), { provide: LocationsApiService, useValue: api }],
    }).compileComponents();
  });

  it('shows a conflict message when the location still has plants', async () => {
    api.delete.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409, statusText: 'Conflict' })),
    );

    const fixture = TestBed.createComponent(LocationsPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const deleteButton = [...compiled.querySelectorAll('button')].find(
      (button) => button.textContent?.trim() === 'Usuń',
    );
    deleteButton?.click();
    fixture.detectChanges();
    const confirmButton = [...compiled.querySelectorAll('button')].find(
      (button) => button.textContent?.trim() === 'Potwierdź',
    );
    confirmButton?.click();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(api.delete).toHaveBeenCalledWith('loc-1');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Nie można usunąć lokalizacji, bo są do niej przypisane rośliny',
    );
  });
});
