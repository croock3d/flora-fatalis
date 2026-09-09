package com.crooked.florafatalis.location.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListLocationsUseCaseHandlerTest {

  @Mock private LocationRepository locationRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private ListLocationsUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler = new ListLocationsUseCaseHandler(locationRepository, activeHouseholdPort);
  }

  @Test
  void listsLocationsForActiveHousehold() {
    Location salon = Location.create(householdId, "Salon", LocationKind.INDOOR);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(locationRepository.findByHousehold(householdId)).willReturn(List.of(salon));

    assertThat(handler.list(userId)).containsExactly(salon);
  }

  @Test
  void noActiveHouseholdReturnsEmptyList() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.empty());

    assertThat(handler.list(userId)).isEmpty();
  }
}
