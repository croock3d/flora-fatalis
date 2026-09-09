package com.crooked.florafatalis.care.application;

import com.crooked.florafatalis.care.application.port.in.WaterPlantUseCase;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaterPlantUseCaseHandler implements WaterPlantUseCase {

  private final PlantRepository plantRepository;
  private final CareEventRepository careEventRepository;
  private final ActiveHouseholdPort activeHouseholdPort;
  private final Clock clock;

  @Override
  public CareEvent water(WaterPlantCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    Plant plant =
        plantRepository.findById(command.plantId()).orElseThrow(PlantNotFoundException::new);
    if (!plant.householdId().equals(householdId) || plant.isArchived()) {
      throw new HouseholdAccessDeniedException();
    }
    CareEvent event =
        CareEvent.watering(
            plant.id(), householdId, command.userId(), Instant.now(clock), command.quantityMl());
    careEventRepository.save(event);
    return event;
  }
}
