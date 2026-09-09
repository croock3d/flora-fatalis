import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';

import { CareApiService } from '../care/care-api.service';
import { CareDashboardDto, DashboardItemDto } from '../care/care.dto';
import { DashboardPage } from './dashboard-page';

describe('DashboardPage', () => {
  let careApi: {
    dashboard: ReturnType<typeof vi.fn>;
    water: ReturnType<typeof vi.fn>;
    fertilize: ReturnType<typeof vi.fn>;
  };

  const overdue: DashboardItemDto = {
    plantId: 'plant-1',
    plantName: 'Monstera',
    careType: 'WATERING',
    dueOn: '2026-01-08',
    overdueDays: 2,
  };
  const dueToday: DashboardItemDto = {
    plantId: 'plant-2',
    plantName: 'Fikus',
    careType: 'FERTILIZING',
    dueOn: '2026-01-10',
    overdueDays: 0,
  };
  const upcoming: DashboardItemDto = {
    plantId: 'plant-3',
    plantName: 'Aloes',
    careType: 'WATERING',
    dueOn: '2026-01-12',
    overdueDays: 0,
  };

  function configure(dashboard: CareDashboardDto) {
    careApi = {
      dashboard: vi.fn().mockReturnValue(of(dashboard)),
      water: vi.fn().mockReturnValue(of({})),
      fertilize: vi.fn().mockReturnValue(of({})),
    };
    return TestBed.configureTestingModule({
      imports: [DashboardPage],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: CareApiService, useValue: careApi },
      ],
    }).compileComponents();
  }

  it('renders overdue, today and upcoming tasks from the backend', async () => {
    await configure({ overdue: [overdue], dueToday: [dueToday], upcoming: [upcoming] });
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Co mam dziś zrobić?');
    expect(text).toContain('Zaległe');
    expect(text).toContain('Monstera');
    expect(text).toContain('2 dni zaległości');
    expect(text).toContain('Fikus');
    expect(text).toContain('nawożenie');
    expect(text).toContain('Aloes');
    expect(text).toContain('Podlej');
    expect(text).toContain('Nawieź');
    expect(text).not.toContain('Wszystko na bieżąco');
  });

  it('shows a positive message when nothing is overdue', async () => {
    await configure({ overdue: [], dueToday: [dueToday], upcoming: [] });
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Wszystko na bieżąco — nic nie zalega.');
    expect(text).toContain('Fikus');
    expect(text).not.toContain('Zaległe');
  });

  it('shows an empty state when there are no tasks', async () => {
    await configure({ overdue: [], dueToday: [], upcoming: [] });
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Nie masz zaplanowanych zadań na najbliższe dni.');
    expect(compiled.querySelectorAll('.dash__card').length).toBe(0);
  });

  it('links plant names to plant details', async () => {
    await configure({ overdue: [overdue], dueToday: [], upcoming: [] });
    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const link = (fixture.nativeElement as HTMLElement).querySelector(
      'a.dash__plant',
    ) as HTMLAnchorElement;
    expect(link.textContent?.trim()).toBe('Monstera');
    expect(link.getAttribute('href')).toBe('/plants/plant-1');
  });

  it('reloads the dashboard after watering', async () => {
    const afterWater: CareDashboardDto = { overdue: [], dueToday: [], upcoming: [upcoming] };
    const water$ = new Subject<unknown>();
    await configure({ overdue: [overdue], dueToday: [], upcoming: [] });
    careApi.water.mockReturnValue(water$.asObservable());
    careApi.dashboard.mockReturnValueOnce(of({ overdue: [overdue], dueToday: [], upcoming: [] }));
    careApi.dashboard.mockReturnValueOnce(of(afterWater));

    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const button = [...(fixture.nativeElement as HTMLElement).querySelectorAll('button')].find(
      (item) => item.textContent?.includes('Podlej'),
    );
    button?.click();
    expect(careApi.water).toHaveBeenCalledWith('plant-1', null);

    careApi.dashboard.mockReturnValue(of(afterWater));
    water$.next({});
    water$.complete();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(careApi.dashboard).toHaveBeenCalledTimes(2);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Aloes');
    expect((fixture.nativeElement as HTMLElement).textContent).not.toContain('Monstera');
  });

  it('reloads the dashboard after fertilizing', async () => {
    const afterFertilize: CareDashboardDto = { overdue: [], dueToday: [], upcoming: [] };
    await configure({ overdue: [], dueToday: [dueToday], upcoming: [] });
    careApi.fertilize.mockReturnValue(of({}));
    careApi.dashboard
      .mockReturnValueOnce(of({ overdue: [], dueToday: [dueToday], upcoming: [] }))
      .mockReturnValueOnce(of(afterFertilize));

    const fixture = TestBed.createComponent(DashboardPage);
    await fixture.whenStable();
    fixture.detectChanges();

    const button = [...(fixture.nativeElement as HTMLElement).querySelectorAll('button')].find(
      (item) => item.textContent?.includes('Nawieź'),
    );
    button?.click();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(careApi.fertilize).toHaveBeenCalledWith('plant-2');
    expect(careApi.dashboard.mock.calls.length).toBeGreaterThanOrEqual(2);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain(
      'Nie masz zaplanowanych zadań na najbliższe dni.',
    );
  });
});
