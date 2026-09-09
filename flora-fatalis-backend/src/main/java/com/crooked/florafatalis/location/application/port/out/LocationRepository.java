package com.crooked.florafatalis.location.application.port.out;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import java.util.List;
import java.util.Optional;

public interface LocationRepository {

  void save(Location location);

  Optional<Location> findById(LocationId id);

  List<Location> findByHousehold(HouseholdId householdId);

  void delete(LocationId id);
}
