export interface CareEventDto {
  id: string;
  plantId: string;
  careType: string;
  performedAt: string;
  performedBy: string;
  performedByName: string;
  quantityMl: number | null;
}

export interface DashboardItemDto {
  plantId: string;
  plantName: string;
  careType: 'WATERING' | 'FERTILIZING';
  dueOn: string;
  overdueDays: number;
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
