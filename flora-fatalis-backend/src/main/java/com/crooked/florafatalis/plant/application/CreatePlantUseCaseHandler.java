package com.crooked.florafatalis.plant.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationNotFoundException;
import com.crooked.florafatalis.plant.application.port.in.CreatePlantUseCase;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.SpeciesNotFoundException;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePlantUseCaseHandler implements CreatePlantUseCase {

  private final PlantRepository plantRepository;
  private final SpeciesRepository speciesRepository;
  private final LocationRepository locationRepository;
  private final ActiveHouseholdPort activeHouseholdPort;
  private final Clock clock;

  @Override
  @Transactional
  public Plant create(CreatePlantCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    speciesRepository.findById(command.speciesId()).orElseThrow(SpeciesNotFoundException::new);
    Location location =
        locationRepository
            .findById(command.locationId())
            .orElseThrow(LocationNotFoundException::new);
    if (!location.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    Plant plant =
        Plant.create(
            householdId,
            command.speciesId(),
            command.locationId(),
            command.name(),
            command.wateringIntervalDaysOverride(),
            command.fertilizingIntervalDaysOverride(),
            command.acquiredAt(),
            command.userId(),
            Instant.now(clock));
    plantRepository.save(plant);
    return plant;
  }
}
