import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { LocationDto, LocationRequest } from './location.dto';

@Injectable({ providedIn: 'root' })
export class LocationsApiService {
  private readonly http = inject(HttpClient);

  list(): Observable<LocationDto[]> {
    return this.http.get<LocationDto[]>('/api/locations');
  }

  create(request: LocationRequest): Observable<LocationDto> {
    return this.http.post<LocationDto>('/api/locations', request);
  }

  update(id: string, request: LocationRequest): Observable<LocationDto> {
    return this.http.put<LocationDto>(`/api/locations/${id}`, request);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`/api/locations/${id}`);
  }
}
