package com.crooked.florafatalis.care.domain;

import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.species.domain.Species;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

public class IntervalCarePolicy implements CarePolicy {

  @Override
  public CareRecommendation recommend(
      Plant plant, Species species, Optional<Instant> lastEventAt, LocalDate today, ZoneId zone) {
    int intervalDays =
        plant.wateringIntervalDaysOverride() != null
            ? plant.wateringIntervalDaysOverride()
            : species.defaultWateringIntervalDays();
    LocalDate lastDate =
        lastEventAt.map(instant -> instant.atZone(zone).toLocalDate()).orElse(today);
    return new CareRecommendation(lastDate.plusDays(intervalDays), intervalDays);
  }
}
