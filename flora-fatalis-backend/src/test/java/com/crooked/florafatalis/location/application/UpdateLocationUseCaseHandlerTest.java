package com.crooked.florafatalis.location.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.application.port.in.UpdateLocationUseCase.UpdateLocationCommand;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.location.domain.LocationNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateLocationUseCaseHandlerTest {

  @Mock private LocationRepository locationRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private UpdateLocationUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler = new UpdateLocationUseCaseHandler(locationRepository, activeHouseholdPort);
  }

  @Test
  void updatesLocationInOwnHousehold() {
    Location existing = Location.create(householdId, "Salon", LocationKind.INDOOR);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(locationRepository.findById(existing.id())).willReturn(Optional.of(existing));

    handler.update(new UpdateLocationCommand(userId, existing.id(), "Kuchnia", "INDOOR"));

    ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
    then(locationRepository).should().save(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Kuchnia");
    assertThat(captor.getValue().id()).isEqualTo(existing.id());
  }

  @Test
  void locationFromOtherHouseholdThrows() {
    Location existing = Location.create(HouseholdId.newId(), "Salon", LocationKind.INDOOR);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(locationRepository.findById(existing.id())).willReturn(Optional.of(existing));

    assertThatThrownBy(
            () ->
                handler.update(
                    new UpdateLocationCommand(userId, existing.id(), "Kuchnia", "INDOOR")))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  @Test
  void missingLocationThrows() {
    LocationId locationId = LocationId.newId();
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(locationRepository.findById(locationId)).willReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                handler.update(new UpdateLocationCommand(userId, locationId, "Kuchnia", "INDOOR")))
        .isInstanceOf(LocationNotFoundException.class);
  }
}
