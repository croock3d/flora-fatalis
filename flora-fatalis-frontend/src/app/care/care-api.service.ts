import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CareDashboardDto, CareEventDto } from './care.dto';

@Injectable({ providedIn: 'root' })
export class CareApiService {
  private readonly http = inject(HttpClient);

  dashboard(): Observable<CareDashboardDto> {
    return this.http.get<CareDashboardDto>('/api/dashboard');
  }

  water(plantId: string, quantityMl?: number | null): Observable<CareEventDto> {
    const body = quantityMl != null ? { quantityMl } : {};
    return this.http.post<CareEventDto>(`/api/plants/${plantId}/water`, body);
  }

  history(plantId: string): Observable<CareEventDto[]> {
    return this.http.get<CareEventDto[]>(`/api/plants/${plantId}/watering-history`);
  }

  deleteEvent(eventId: string): Observable<void> {
    return this.http.delete<void>(`/api/care-events/${eventId}`);
  }
}
