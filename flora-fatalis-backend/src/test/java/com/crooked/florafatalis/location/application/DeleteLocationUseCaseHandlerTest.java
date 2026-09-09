package com.crooked.florafatalis.location.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.application.port.in.DeleteLocationUseCase.DeleteLocationCommand;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationInUseException;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteLocationUseCaseHandlerTest {

  @Mock private LocationRepository locationRepository;
  @Mock private PlantRepository plantRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private DeleteLocationUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new DeleteLocationUseCaseHandler(locationRepository, plantRepository, activeHouseholdPort);
  }

  @Test
  void deletesLocationInOwnHousehold() {
    Location existing = Location.create(householdId, "Salon", LocationKind.INDOOR);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(locationRepository.findById(existing.id())).willReturn(Optional.of(existing));
    given(plantRepository.existsActiveByLocation(existing.id())).willReturn(false);

    handler.delete(new DeleteLocationCommand(userId, existing.id()));

    then(locationRepository).should().delete(existing.id());
  }

  @Test
  void locationFromOtherHouseholdThrows() {
    Location existing = Location.create(HouseholdId.newId(), "Salon", LocationKind.INDOOR);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(locationRepository.findById(existing.id())).willReturn(Optional.of(existing));

    assertThatThrownBy(() -> handler.delete(new DeleteLocationCommand(userId, existing.id())))
        .isInstanceOf(HouseholdAccessDeniedException.class);
    then(locationRepository).should().findById(existing.id());
    then(locationRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  void locationWithActivePlantsThrows() {
    Location existing = Location.create(householdId, "Salon", LocationKind.INDOOR);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(locationRepository.findById(existing.id())).willReturn(Optional.of(existing));
    given(plantRepository.existsActiveByLocation(existing.id())).willReturn(true);

    assertThatThrownBy(() -> handler.delete(new DeleteLocationCommand(userId, existing.id())))
        .isInstanceOf(LocationInUseException.class);
    then(locationRepository).should(never()).delete(existing.id());
  }
}
