import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { HouseholdStatusResponse, SendInvitationRequest } from './household.dto';

@Injectable({ providedIn: 'root' })
export class HouseholdApiService {
  private readonly http = inject(HttpClient);

  getStatus(): Observable<HouseholdStatusResponse> {
    return this.http.get<HouseholdStatusResponse>('/api/household/status');
  }

  sendInvitation(email: string): Observable<void> {
    const body: SendInvitationRequest = { inviteeEmail: email };
    return this.http.post<void>('/api/household/invite', body);
  }

  accept(invitationId: string): Observable<void> {
    return this.http.post<void>(`/api/household/invitations/${invitationId}/accept`, null);
  }

  reject(invitationId: string): Observable<void> {
    return this.http.post<void>(`/api/household/invitations/${invitationId}/reject`, null);
  }

  switchActive(householdId: string): Observable<void> {
    return this.http.post<void>(`/api/household/${householdId}/switch`, null);
  }

  leave(householdId: string): Observable<void> {
    return this.http.delete<void>(`/api/household/${householdId}`);
  }
}
