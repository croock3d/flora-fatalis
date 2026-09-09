package com.crooked.florafatalis.care.application.port.in;

import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.time.LocalDate;

public interface GetPlantCareStatusUseCase {

  PlantCareStatus get(UserId userId, PlantId plantId);

  record PlantCareStatus(CareTypeStatus watering, CareTypeStatus fertilizing) {}

  record CareTypeStatus(Instant lastAt, LocalDate nextOn, Integer intervalDays) {}
}
