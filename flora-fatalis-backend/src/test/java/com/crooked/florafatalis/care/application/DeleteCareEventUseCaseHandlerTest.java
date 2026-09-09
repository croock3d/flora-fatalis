package com.crooked.florafatalis.care.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.care.application.port.in.DeleteCareEventUseCase.DeleteCareEventCommand;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareEventId;
import com.crooked.florafatalis.care.domain.CareEventNotFoundException;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteCareEventUseCaseHandlerTest {

  @Mock private CareEventRepository careEventRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private DeleteCareEventUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler = new DeleteCareEventUseCaseHandler(careEventRepository, activeHouseholdPort);
  }

  @Test
  void deletesEventInOwnHousehold() {
    CareEvent event =
        CareEvent.watering(
            PlantId.newId(), householdId, userId, Instant.parse("2026-01-10T08:00:00Z"), null);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(careEventRepository.findById(event.id())).willReturn(Optional.of(event));

    handler.delete(new DeleteCareEventCommand(userId, event.id()));

    then(careEventRepository).should().delete(event.id());
  }

  @Test
  void eventFromOtherHouseholdThrows() {
    CareEvent event =
        CareEvent.watering(
            PlantId.newId(),
            HouseholdId.newId(),
            userId,
            Instant.parse("2026-01-10T08:00:00Z"),
            null);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(careEventRepository.findById(event.id())).willReturn(Optional.of(event));

    assertThatThrownBy(() -> handler.delete(new DeleteCareEventCommand(userId, event.id())))
        .isInstanceOf(HouseholdAccessDeniedException.class);
    then(careEventRepository).should().findById(event.id());
    then(careEventRepository).shouldHaveNoMoreInteractions();
  }

  @Test
  void missingEventThrows() {
    CareEventId eventId = CareEventId.newId();
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(careEventRepository.findById(eventId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> handler.delete(new DeleteCareEventCommand(userId, eventId)))
        .isInstanceOf(CareEventNotFoundException.class);
  }
}
