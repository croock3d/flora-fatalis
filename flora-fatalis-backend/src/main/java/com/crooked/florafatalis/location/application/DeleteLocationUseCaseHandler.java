package com.crooked.florafatalis.location.application;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.in.DeleteLocationUseCase;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteLocationUseCaseHandler implements DeleteLocationUseCase {

  private final LocationRepository locationRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
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
    locationRepository.delete(command.locationId());
  }
}
