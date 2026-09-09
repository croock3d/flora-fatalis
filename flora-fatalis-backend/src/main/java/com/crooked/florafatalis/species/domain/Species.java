package com.crooked.florafatalis.species.domain;

import java.util.Objects;

public record Species(
    SpeciesId id,
    String name,
    String latinName,
    int defaultWateringIntervalDays,
    Integer wateringIntervalMinDays,
    Integer wateringIntervalMaxDays,
    String wateringIntervalLabel,
    String lightPreference,
    String humidityPreference,
    String category,
    Integer defaultFertilizingIntervalDays) {

  public Species {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (defaultWateringIntervalDays < 1) {
      throw new IllegalArgumentException("defaultWateringIntervalDays must be at least 1");
    }
    if (defaultFertilizingIntervalDays != null && defaultFertilizingIntervalDays < 1) {
      throw new IllegalArgumentException("defaultFertilizingIntervalDays must be at least 1");
    }
  }
}
