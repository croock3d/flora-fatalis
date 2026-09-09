package com.crooked.florafatalis.care.application.port.in;

import com.crooked.florafatalis.care.domain.CareEventId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface DeleteCareEventUseCase {

  void delete(DeleteCareEventCommand command);

  record DeleteCareEventCommand(UserId userId, CareEventId eventId) {}
}
