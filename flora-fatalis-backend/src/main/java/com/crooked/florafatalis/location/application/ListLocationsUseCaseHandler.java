package com.crooked.florafatalis.location.application;

import com.crooked.florafatalis.location.application.port.in.ListLocationsUseCase;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListLocationsUseCaseHandler implements ListLocationsUseCase {

  private final LocationRepository locationRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public List<Location> list(UserId userId) {
    return activeHouseholdPort
        .findActiveHouseholdId(userId)
        .map(locationRepository::findByHousehold)
        .orElse(List.of());
  }
}
