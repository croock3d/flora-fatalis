package com.crooked.florafatalis.care.application.port.in;

import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface FertilizePlantUseCase {

  CareEvent fertilize(FertilizePlantCommand command);

  record FertilizePlantCommand(UserId userId, PlantId plantId) {}
}
