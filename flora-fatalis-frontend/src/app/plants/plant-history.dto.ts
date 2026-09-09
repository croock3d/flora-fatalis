export type PlantHistoryType = 'WATERING' | 'FERTILIZING' | 'PHOTO' | 'CREATED';

export interface PlantHistoryItemDto {
  type: PlantHistoryType;
  occurredAt: string;
  sourceId: string;
  quantityMl: number | null;
  performedBy: string | null;
  performedByName: string | null;
  photoUrl: string | null;
}

export interface PlantHistoryDayGroup {
  key: string;
  label: string;
  items: PlantHistoryItemDto[];
}
