package com.crooked.florafatalis.plant.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.plant.application.port.in.ArchivePlantUseCase;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArchivePlantUseCaseHandler implements ArchivePlantUseCase {

  private final PlantRepository plantRepository;
  private final ActiveHouseholdPort activeHouseholdPort;
  private final Clock clock;

  @Override
  @Transactional
  public void archive(ArchivePlantCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    Plant existing =
        plantRepository.findById(command.plantId()).orElseThrow(PlantNotFoundException::new);
    if (!existing.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    plantRepository.save(existing.archive(Instant.now(clock)));
  }
}
