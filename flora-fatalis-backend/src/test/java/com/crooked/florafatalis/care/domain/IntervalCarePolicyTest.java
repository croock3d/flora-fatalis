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

class IntervalCarePolicyTest {

  private final IntervalCarePolicy policy = new IntervalCarePolicy();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final Species species =
      new Species(speciesId, "Inny", null, 7, null, null, null, null, null, null, null);
  private final LocalDate today = LocalDate.parse("2026-01-10");

  @Test
  void usesSpeciesIntervalWhenNoOverride() {
    Plant plant = plant(null);
    Instant last = Instant.parse("2026-01-03T12:00:00Z");

    CareRecommendation recommendation =
        policy.recommend(plant, species, Optional.of(last), today, ZoneOffset.UTC);

    assertThat(recommendation.intervalDays()).isEqualTo(7);
    assertThat(recommendation.dueOn()).isEqualTo(LocalDate.parse("2026-01-10"));
  }

  @Test
  void usesPlantOverride() {
    Plant plant = plant(3);
    Instant last = Instant.parse("2026-01-08T12:00:00Z");

    CareRecommendation recommendation =
        policy.recommend(plant, species, Optional.of(last), today, ZoneOffset.UTC);

    assertThat(recommendation.intervalDays()).isEqualTo(3);
    assertThat(recommendation.dueOn()).isEqualTo(LocalDate.parse("2026-01-11"));
  }

  @Test
  void withoutHistoryDueIsTodayPlusInterval() {
    Plant plant = plant(null);

    CareRecommendation recommendation =
        policy.recommend(plant, species, Optional.empty(), today, ZoneOffset.UTC);

    assertThat(recommendation.dueOn()).isEqualTo(LocalDate.parse("2026-01-17"));
  }

  private Plant plant(Integer override) {
    return Plant.create(
        HouseholdId.newId(),
        speciesId,
        LocationId.newId(),
        "Monstera",
        override,
        null,
        null,
        UserId.newId(),
        Instant.parse("2026-01-01T00:00:00Z"));
  }
}
