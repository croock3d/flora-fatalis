package com.crooked.florafatalis.location.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.in.UpdateLocationUseCase;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.location.domain.LocationNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateLocationUseCaseHandler implements UpdateLocationUseCase {

  private final LocationRepository locationRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public Location update(UpdateLocationCommand command) {
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
    Location updated = existing.rename(command.name(), LocationKind.valueOf(command.kind()));
    locationRepository.save(updated);
    return updated;
  }
}
