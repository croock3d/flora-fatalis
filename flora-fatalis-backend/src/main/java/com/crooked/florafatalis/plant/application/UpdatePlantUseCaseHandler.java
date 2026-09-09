package com.crooked.florafatalis.plant.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationNotFoundException;
import com.crooked.florafatalis.plant.application.port.in.UpdatePlantUseCase;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.SpeciesNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdatePlantUseCaseHandler implements UpdatePlantUseCase {

  private final PlantRepository plantRepository;
  private final SpeciesRepository speciesRepository;
  private final LocationRepository locationRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public Plant update(UpdatePlantCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    Plant existing =
        plantRepository.findById(command.plantId()).orElseThrow(PlantNotFoundException::new);
    if (!existing.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    speciesRepository.findById(command.speciesId()).orElseThrow(SpeciesNotFoundException::new);
    Location location =
        locationRepository
            .findById(command.locationId())
            .orElseThrow(LocationNotFoundException::new);
    if (!location.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    Plant updated =
        existing.update(
            command.speciesId(),
            command.locationId(),
            command.name(),
            command.wateringIntervalDaysOverride(),
            command.acquiredAt());
    plantRepository.save(updated);
    return updated;
  }
}
