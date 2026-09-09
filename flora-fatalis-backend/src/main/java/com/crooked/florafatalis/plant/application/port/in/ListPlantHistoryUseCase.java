package com.crooked.florafatalis.plant.application.port.in;

import com.crooked.florafatalis.care.domain.PruningKind;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ListPlantHistoryUseCase {

  List<PlantHistoryItem> list(UserId userId, PlantId plantId);

  enum HistoryType {
    WATERING,
    FERTILIZING,
    PRUNING,
    PHOTO,
    CREATED
  }

  record PlantHistoryItem(
      HistoryType type,
      Instant occurredAt,
      UUID sourceId,
      Integer quantityMl,
      UserId performedBy,
      String notes,
      PruningKind pruningKind) {}
}
