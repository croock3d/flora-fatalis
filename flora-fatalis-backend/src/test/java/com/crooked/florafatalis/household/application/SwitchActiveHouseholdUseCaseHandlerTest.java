package com.crooked.florafatalis.household.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.application.port.in.SwitchActiveHouseholdUseCase.SwitchActiveHouseholdCommand;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdNotFoundException;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SwitchActiveHouseholdUseCaseHandlerTest {

  @Mock HouseholdRepository householdRepository;
  @Mock UserActiveHouseholdPort userActiveHouseholdPort;

  SwitchActiveHouseholdUseCaseHandler handler;

  private final UserId userId = new UserId(UUID.randomUUID());
  private final UserId ownerId = new UserId(UUID.randomUUID());

  @BeforeEach
  void setUp() {
    handler = new SwitchActiveHouseholdUseCaseHandler(householdRepository, userActiveHouseholdPort);
  }

  @Test
  void switchesWhenUserIsMember() {
    HouseholdId householdId = HouseholdId.newId();
    Household household = Household.createForOwner(householdId, ownerId).join(userId);
    given(householdRepository.findById(householdId)).willReturn(Optional.of(household));

    handler.switchTo(new SwitchActiveHouseholdCommand(householdId, userId));

    then(userActiveHouseholdPort).should().set(userId, householdId);
  }

  @Test
  void throwsWhenHouseholdMissing() {
    HouseholdId householdId = HouseholdId.newId();
    given(householdRepository.findById(householdId)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> handler.switchTo(new SwitchActiveHouseholdCommand(householdId, userId)))
        .isInstanceOf(HouseholdNotFoundException.class);
  }

  @Test
  void throwsWhenUserIsNotMember() {
    HouseholdId householdId = HouseholdId.newId();
    Household household = Household.createForOwner(householdId, ownerId);
    given(householdRepository.findById(householdId)).willReturn(Optional.of(household));

    assertThatThrownBy(
            () -> handler.switchTo(new SwitchActiveHouseholdCommand(householdId, userId)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }
}
