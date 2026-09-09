package com.crooked.florafatalis.plant.application.port.in;

import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface GetPlantUseCase {

  Plant get(GetPlantCommand command);

  record GetPlantCommand(UserId userId, PlantId plantId) {}
}
