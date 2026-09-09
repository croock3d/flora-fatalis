import { PruningKind } from '../care/care.dto';

export type PlantHistoryType = 'WATERING' | 'FERTILIZING' | 'PRUNING' | 'PHOTO' | 'CREATED';

export interface PlantHistoryItemDto {
  type: PlantHistoryType;
  occurredAt: string;
  sourceId: string;
  quantityMl: number | null;
  performedBy: string | null;
  performedByName: string | null;
  photoUrl: string | null;
  notes: string | null;
  pruningKind: PruningKind | null;
}

export interface PlantHistoryDayGroup {
  key: string;
  label: string;
  items: PlantHistoryItemDto[];
}
