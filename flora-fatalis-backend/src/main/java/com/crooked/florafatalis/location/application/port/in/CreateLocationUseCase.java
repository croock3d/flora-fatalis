package com.crooked.florafatalis.location.application.port.in;

import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.shared.domain.UserId;

public interface CreateLocationUseCase {

  Location create(CreateLocationCommand command);

  record CreateLocationCommand(UserId userId, String name, String kind) {}
}
