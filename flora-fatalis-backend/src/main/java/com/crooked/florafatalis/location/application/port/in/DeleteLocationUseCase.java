package com.crooked.florafatalis.location.application.port.in;

import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface DeleteLocationUseCase {

  void delete(DeleteLocationCommand command);

  record DeleteLocationCommand(UserId userId, LocationId locationId) {}
}
