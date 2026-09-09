package com.crooked.florafatalis.location.application.port.in;

import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface UpdateLocationUseCase {

  Location update(UpdateLocationCommand command);

  record UpdateLocationCommand(UserId userId, LocationId locationId, String name, String kind) {}
}
