import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { SpeciesDto } from './species.dto';

@Injectable({ providedIn: 'root' })
export class SpeciesApiService {
  private readonly http = inject(HttpClient);

  list(): Observable<SpeciesDto[]> {
    return this.http.get<SpeciesDto[]>('/api/species');
  }
}
