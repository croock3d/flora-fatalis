export interface PlantDto {
  id: string;
  speciesId: string;
  locationId: string;
  name: string;
  wateringIntervalDaysOverride: number | null;
  acquiredAt: string | null;
  archivedAt: string | null;
  createdAt: string;
}

export interface PlantRequest {
  speciesId: string;
  locationId: string;
  name: string;
  wateringIntervalDaysOverride: number | null;
  acquiredAt: string | null;
}
