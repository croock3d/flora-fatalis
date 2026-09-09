package com.crooked.florafatalis.care.application.port.in;

import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;

public interface ListWateringHistoryUseCase {

  List<CareEvent> list(UserId userId, PlantId plantId);
}
