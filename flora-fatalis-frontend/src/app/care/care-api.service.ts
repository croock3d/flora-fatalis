import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CareDashboardDto, CareEventDto, PlantCareStatusDto, PrunePlantRequest } from './care.dto';

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

  fertilize(plantId: string): Observable<CareEventDto> {
    return this.http.post<CareEventDto>(`/api/plants/${plantId}/fertilize`, {});
  }

  prune(plantId: string, request: PrunePlantRequest = {}): Observable<CareEventDto> {
    return this.http.post<CareEventDto>(`/api/plants/${plantId}/prune`, request);
  }

  careStatus(plantId: string): Observable<PlantCareStatusDto> {
    return this.http.get<PlantCareStatusDto>(`/api/plants/${plantId}/care-status`);
  }

  deleteEvent(eventId: string): Observable<void> {
    return this.http.delete<void>(`/api/care-events/${eventId}`);
  }
}
