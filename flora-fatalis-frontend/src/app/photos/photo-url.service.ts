import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PhotoUrlService {
  private readonly http = inject(HttpClient);
  private readonly urls = new Map<string, string>();

  objectUrl(apiUrl: string): Observable<string> {
    const cached = this.urls.get(apiUrl);
    if (cached) {
      return new Observable((subscriber) => {
        subscriber.next(cached);
        subscriber.complete();
      });
    }
    return this.http.get(apiUrl, { responseType: 'blob' }).pipe(
      map((blob) => {
        const url = URL.createObjectURL(blob);
        this.urls.set(apiUrl, url);
        return url;
      }),
    );
  }
}
