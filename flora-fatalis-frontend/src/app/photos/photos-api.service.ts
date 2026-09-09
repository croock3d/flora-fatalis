import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PlantPhotoDto } from './photo.dto';

@Injectable({ providedIn: 'root' })
export class PhotosApiService {
  private readonly http = inject(HttpClient);

  list(plantId: string): Observable<PlantPhotoDto[]> {
    return this.http.get<PlantPhotoDto[]>(`/api/plants/${plantId}/photos`);
  }

  upload(plantId: string, file: File): Observable<PlantPhotoDto> {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.http.post<PlantPhotoDto>(`/api/plants/${plantId}/photos`, body);
  }

  setPrimary(photoId: string): Observable<void> {
    return this.http.post<void>(`/api/photos/${photoId}/primary`, null);
  }

  delete(photoId: string): Observable<void> {
    return this.http.delete<void>(`/api/photos/${photoId}`);
  }
}
