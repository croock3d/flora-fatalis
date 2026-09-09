export interface SpeciesDto {
  id: string;
  name: string;
  latinName: string | null;
  defaultWateringIntervalDays: number;
  wateringIntervalMinDays: number | null;
  wateringIntervalMaxDays: number | null;
  wateringIntervalLabel: string | null;
  lightPreference: string | null;
  humidityPreference: string | null;
  category: string | null;
  defaultFertilizingIntervalDays: number | null;
}
