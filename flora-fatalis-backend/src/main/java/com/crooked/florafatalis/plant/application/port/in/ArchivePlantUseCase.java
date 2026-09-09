package com.crooked.florafatalis.plant.application.port.in;

import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface ArchivePlantUseCase {

  void archive(ArchivePlantCommand command);

  record ArchivePlantCommand(UserId userId, PlantId plantId) {}
}
