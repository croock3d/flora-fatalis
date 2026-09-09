package com.crooked.florafatalis.location.application.port.in;

import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;

public interface ListLocationsUseCase {

  List<Location> list(UserId userId);
}
