package com.crooked.florafatalis.care.domain;

import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.species.domain.Species;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

public class FertilizingCarePolicy {

  public Optional<CareRecommendation> recommend(
      Plant plant, Species species, Optional<Instant> lastEventAt, LocalDate today, ZoneId zone) {
    Integer intervalDays =
        plant.fertilizingIntervalDaysOverride() != null
            ? plant.fertilizingIntervalDaysOverride()
            : species.defaultFertilizingIntervalDays();
    if (intervalDays == null) {
      return Optional.empty();
    }
    LocalDate lastDate =
        lastEventAt.map(instant -> instant.atZone(zone).toLocalDate()).orElse(today);
    return Optional.of(new CareRecommendation(lastDate.plusDays(intervalDays), intervalDays));
  }
}
