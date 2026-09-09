export type LocationKind = 'INDOOR' | 'BALCONY' | 'OUTDOOR';

export interface LocationDto {
  id: string;
  name: string;
  kind: LocationKind;
}

export interface LocationRequest {
  name: string;
  kind: LocationKind;
}
