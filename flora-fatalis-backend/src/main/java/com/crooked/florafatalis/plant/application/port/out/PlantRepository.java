package com.crooked.florafatalis.plant.application.port.out;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import java.util.List;
import java.util.Optional;

public interface PlantRepository {

  void save(Plant plant);

  Optional<Plant> findById(PlantId id);

  List<Plant> findActiveByHousehold(HouseholdId householdId);

  boolean existsActiveByLocation(LocationId locationId);
}
