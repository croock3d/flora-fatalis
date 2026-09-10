package com.crooked.florafatalis.location.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.in.DeleteLocationUseCase;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationInUseException;
import com.crooked.florafatalis.location.domain.LocationNotFoundException;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteLocationUseCaseHandler implements DeleteLocationUseCase {

  private final LocationRepository locationRepository;
  private final PlantRepository plantRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  @Transactional
  public void delete(DeleteLocationCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    Location existing =
        locationRepository
            .findById(command.locationId())
            .orElseThrow(LocationNotFoundException::new);
    if (!existing.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    if (plantRepository.existsActiveByLocation(existing.id())) {
      throw new LocationInUseException();
    }
    locationRepository.delete(command.locationId());
  }
}
