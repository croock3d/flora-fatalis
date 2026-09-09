package com.crooked.florafatalis.care.domain;

import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.species.domain.Species;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

public interface CarePolicy {

  CareRecommendation recommend(
      Plant plant, Species species, Optional<Instant> lastEventAt, LocalDate today, ZoneId zone);
}
