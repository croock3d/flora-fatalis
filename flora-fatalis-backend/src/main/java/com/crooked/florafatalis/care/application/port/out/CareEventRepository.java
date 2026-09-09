package com.crooked.florafatalis.care.application.port.out;

import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareEventId;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.plant.domain.PlantId;
import java.util.List;
import java.util.Optional;

public interface CareEventRepository {

  void save(CareEvent event);

  void delete(CareEventId id);

  Optional<CareEvent> findById(CareEventId id);

  List<CareEvent> findByPlantIdAndCareType(PlantId plantId, CareType careType);

  Optional<CareEvent> findLatest(PlantId plantId, CareType careType);

  List<CareEvent> findLatestByHouseholdAndCareType(HouseholdId householdId, CareType careType);
}
