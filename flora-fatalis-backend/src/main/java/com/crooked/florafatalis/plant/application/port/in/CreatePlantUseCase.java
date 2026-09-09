package com.crooked.florafatalis.plant.application.port.in;

import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;

public interface CreatePlantUseCase {

  Plant create(CreatePlantCommand command);

  record CreatePlantCommand(
      UserId userId,
      SpeciesId speciesId,
      LocationId locationId,
      String name,
      Integer wateringIntervalDaysOverride,
      Integer fertilizingIntervalDaysOverride,
      Instant acquiredAt) {}
}
