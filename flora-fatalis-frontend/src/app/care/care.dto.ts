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
  dueOn: string;
  overdueDays: number;
}

export interface CareDashboardDto {
  overdue: DashboardItemDto[];
  dueToday: DashboardItemDto[];
  upcoming: DashboardItemDto[];
}
