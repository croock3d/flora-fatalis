package com.crooked.florafatalis.photo.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PlantPhotoAccess {

  private final PlantRepository plantRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  Plant requirePlant(UserId userId, PlantId plantId) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(userId)
            .orElseThrow(NoActiveHouseholdException::new);
    Plant plant = plantRepository.findById(plantId).orElseThrow(PlantNotFoundException::new);
    if (!plant.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    return plant;
  }
}
