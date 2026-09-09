package com.crooked.florafatalis.care.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class FertilizingCarePolicyTest {

  private final FertilizingCarePolicy policy = new FertilizingCarePolicy();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final LocalDate today = LocalDate.parse("2026-01-10");

  @Test
  void usesSpeciesIntervalWhenNoOverride() {
    Species species = species(30);
    Plant plant = plant(null);

    Optional<CareRecommendation> recommendation =
        policy.recommend(
            plant,
            species,
            Optional.of(Instant.parse("2025-12-11T12:00:00Z")),
            today,
            ZoneOffset.UTC);

    assertThat(recommendation).isPresent();
    assertThat(recommendation.get().intervalDays()).isEqualTo(30);
    assertThat(recommendation.get().dueOn()).isEqualTo(LocalDate.parse("2026-01-10"));
  }

  @Test
  void usesPlantOverride() {
    Species species = species(30);
    Plant plant = plant(45);

    Optional<CareRecommendation> recommendation =
        policy.recommend(
            plant,
            species,
            Optional.of(Instant.parse("2025-12-26T12:00:00Z")),
            today,
            ZoneOffset.UTC);

    assertThat(recommendation).isPresent();
    assertThat(recommendation.get().intervalDays()).isEqualTo(45);
    assertThat(recommendation.get().dueOn()).isEqualTo(LocalDate.parse("2026-02-09"));
  }

  @Test
  void emptyWhenSpeciesHasNoInterval() {
    Species species = species(null);
    Plant plant = plant(null);

    Optional<CareRecommendation> recommendation =
        policy.recommend(plant, species, Optional.empty(), today, ZoneOffset.UTC);

    assertThat(recommendation).isEmpty();
  }

  private Species species(Integer fertilizingInterval) {
    return new Species(
        speciesId,
        "Inny",
        null,
        7,
        null,
        null,
        null,
        null,
        null,
        null,
        fertilizingInterval,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }

  private Plant plant(Integer override) {
    return Plant.create(
        HouseholdId.newId(),
        speciesId,
        LocationId.newId(),
        "Monstera",
        null,
        override,
        null,
        UserId.newId(),
        Instant.parse("2026-01-01T00:00:00Z"));
  }
}
