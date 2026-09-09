package com.crooked.florafatalis.location.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.location.application.port.in.CreateLocationUseCase.CreateLocationCommand;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationKind;
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
class CreateLocationUseCaseHandlerTest {

  @Mock private LocationRepository locationRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private CreateLocationUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler = new CreateLocationUseCaseHandler(locationRepository, activeHouseholdPort);
  }

  @Test
  void createsLocationForActiveHousehold() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));

    Location created = handler.create(new CreateLocationCommand(userId, "Salon", "INDOOR"));

    ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
    then(locationRepository).should().save(captor.capture());
    assertThat(captor.getValue().name()).isEqualTo("Salon");
    assertThat(captor.getValue().kind()).isEqualTo(LocationKind.INDOOR);
    assertThat(captor.getValue().householdId()).isEqualTo(householdId);
    assertThat(created).isEqualTo(captor.getValue());
  }

  @Test
  void noActiveHouseholdThrows() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> handler.create(new CreateLocationCommand(userId, "Salon", "INDOOR")))
        .isInstanceOf(NoActiveHouseholdException.class);
    then(locationRepository).shouldHaveNoInteractions();
  }
}
