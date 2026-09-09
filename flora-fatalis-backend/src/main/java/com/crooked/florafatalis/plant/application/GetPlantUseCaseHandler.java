package com.crooked.florafatalis.plant.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.plant.application.port.in.GetPlantUseCase;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPlantUseCaseHandler implements GetPlantUseCase {

  private final PlantRepository plantRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public Plant get(GetPlantCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    Plant plant =
        plantRepository.findById(command.plantId()).orElseThrow(PlantNotFoundException::new);
    if (!plant.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    return plant;
  }
}
