package com.crooked.florafatalis.location.application;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.in.CreateLocationUseCase;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateLocationUseCaseHandler implements CreateLocationUseCase {

  private final LocationRepository locationRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public Location create(CreateLocationCommand command) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(command.userId())
            .orElseThrow(NoActiveHouseholdException::new);
    Location location =
        Location.create(householdId, command.name(), LocationKind.valueOf(command.kind()));
    locationRepository.save(location);
    return location;
  }
}
