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
  fertilizingIntervalMinDays: number | null;
  fertilizingIntervalMaxDays: number | null;
  fertilizingIntervalLabel: string | null;
  fertilizingSeason: string | null;
  fertilizerType: string | null;
  fertilizerForm: string | null;
  fertilizingNotes: string | null;
  fertilizingRestPeriod: string | null;
}
