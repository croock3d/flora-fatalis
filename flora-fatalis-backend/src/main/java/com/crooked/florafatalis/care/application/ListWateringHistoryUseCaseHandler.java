package com.crooked.florafatalis.care.application;

import com.crooked.florafatalis.care.application.port.in.ListWateringHistoryUseCase;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListWateringHistoryUseCaseHandler implements ListWateringHistoryUseCase {

  private final PlantRepository plantRepository;
  private final CareEventRepository careEventRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public List<CareEvent> list(UserId userId, PlantId plantId) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(userId)
            .orElseThrow(NoActiveHouseholdException::new);
    Plant plant = plantRepository.findById(plantId).orElseThrow(PlantNotFoundException::new);
    if (!plant.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    return careEventRepository.findByPlantIdAndCareType(plantId, CareType.WATERING);
  }
}
