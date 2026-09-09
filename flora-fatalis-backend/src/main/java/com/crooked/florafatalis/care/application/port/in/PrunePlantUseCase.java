package com.crooked.florafatalis.care.application.port.in;

import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.PruningKind;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.LocalDate;

public interface PrunePlantUseCase {

  CareEvent prune(PrunePlantCommand command);

  record PrunePlantCommand(
      UserId userId,
      PlantId plantId,
      LocalDate performedOn,
      PruningKind pruningKind,
      String notes) {}
}
