export interface PlantDto {
  id: string;
  speciesId: string;
  locationId: string;
  name: string;
  wateringIntervalDaysOverride: number | null;
  fertilizingIntervalDaysOverride: number | null;
  acquiredAt: string | null;
  archivedAt: string | null;
  createdAt: string;
  primaryPhotoUrl?: string | null;
}

export interface PlantRequest {
  speciesId: string;
  locationId: string;
  name: string;
  wateringIntervalDaysOverride: number | null;
  fertilizingIntervalDaysOverride: number | null;
  acquiredAt: string | null;
}
