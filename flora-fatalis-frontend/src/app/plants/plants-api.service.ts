import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PlantHistoryItemDto } from './plant-history.dto';
import { PlantDto, PlantRequest } from './plant.dto';

@Injectable({ providedIn: 'root' })
export class PlantsApiService {
  private readonly http = inject(HttpClient);

  list(): Observable<PlantDto[]> {
    return this.http.get<PlantDto[]>('/api/plants');
  }

  get(id: string): Observable<PlantDto> {
    return this.http.get<PlantDto>(`/api/plants/${id}`);
  }

  history(id: string): Observable<PlantHistoryItemDto[]> {
    return this.http.get<PlantHistoryItemDto[]>(`/api/plants/${id}/history`);
  }

  create(request: PlantRequest): Observable<PlantDto> {
    return this.http.post<PlantDto>('/api/plants', request);
  }

  update(id: string, request: PlantRequest): Observable<PlantDto> {
    return this.http.put<PlantDto>(`/api/plants/${id}`, request);
  }

  archive(id: string): Observable<void> {
    return this.http.delete<void>(`/api/plants/${id}`);
  }
}
