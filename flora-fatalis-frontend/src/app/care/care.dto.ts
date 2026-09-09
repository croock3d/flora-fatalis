export type PruningKind = 'DRY_LEAVES' | 'SHAPING' | 'HEAVY_PRUNING' | 'OTHER';

export interface CareEventDto {
  id: string;
  plantId: string;
  careType: string;
  performedAt: string;
  performedBy: string;
  performedByName: string;
  quantityMl: number | null;
  notes: string | null;
  pruningKind: PruningKind | null;
}

export interface PrunePlantRequest {
  performedOn?: string | null;
  pruningKind?: PruningKind | null;
  notes?: string | null;
}

export interface DashboardItemDto {
  plantId: string;
  plantName: string;
  careType: 'WATERING' | 'FERTILIZING';
  dueOn: string;
  overdueDays: number;
  primaryPhotoUrl?: string | null;
}

export interface CareTypeStatusDto {
  lastAt: string | null;
  nextOn: string | null;
  intervalDays: number | null;
}

export interface PlantCareStatusDto {
  watering: CareTypeStatusDto;
  fertilizing: CareTypeStatusDto | null;
}

export interface CareDashboardDto {
  overdue: DashboardItemDto[];
  dueToday: DashboardItemDto[];
  upcoming: DashboardItemDto[];
}
