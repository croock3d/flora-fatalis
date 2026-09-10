package com.crooked.florafatalis.household.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.application.port.in.LeaveHouseholdUseCase.LeaveHouseholdCommand;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeaveHouseholdUseCaseHandlerTest {

  @Mock HouseholdRepository householdRepository;
  @Mock HouseholdInvitationRepository invitationRepository;
  @Mock UserActiveHouseholdPort userActiveHouseholdPort;

  LeaveHouseholdUseCaseHandler handler;

  private final UserId ownerId = new UserId(UUID.randomUUID());
  private final UserId partnerId = new UserId(UUID.randomUUID());
  private final UserId strangerUserId = new UserId(UUID.randomUUID());

  @BeforeEach
  void setUp() {
    handler =
        new LeaveHouseholdUseCaseHandler(
            householdRepository, invitationRepository, userActiveHouseholdPort);
  }

  @Test
  void partnerLeavingKeepsOwnedHousehold() {
    HouseholdId householdId = HouseholdId.newId();
    Household household = Household.createForOwner(householdId, ownerId).join(partnerId);
    Household ownedByPartner = Household.createForOwner(HouseholdId.newId(), partnerId);
    given(householdRepository.findById(householdId)).willReturn(Optional.of(household));
    given(invitationRepository.findByHouseholdId(householdId)).willReturn(Optional.empty());
    given(userActiveHouseholdPort.get(partnerId)).willReturn(Optional.of(householdId));
    given(householdRepository.findOwnedBy(partnerId)).willReturn(Optional.of(ownedByPartner));

    handler.leave(new LeaveHouseholdCommand(householdId, partnerId));

    ArgumentCaptor<Household> captor = ArgumentCaptor.forClass(Household.class);
    then(householdRepository).should().save(captor.capture());
    assertThat(captor.getValue().ownerId()).isEqualTo(ownerId);
    assertThat(captor.getValue().partnerId()).isNull();
    then(userActiveHouseholdPort).should().set(partnerId, ownedByPartner.id());
  }

  @Test
  void ownerCannotLeaveOwnedHousehold() {
    HouseholdId householdId = HouseholdId.newId();
    Household household = Household.createForOwner(householdId, ownerId).join(partnerId);
    given(householdRepository.findById(householdId)).willReturn(Optional.of(household));

    assertThatThrownBy(() -> handler.leave(new LeaveHouseholdCommand(householdId, ownerId)))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void throwsWhenNoHouseholdFound() {
    HouseholdId householdId = HouseholdId.newId();
    given(householdRepository.findById(householdId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> handler.leave(new LeaveHouseholdCommand(householdId, ownerId)))
        .isInstanceOf(HouseholdNotFoundException.class);
  }

  @Test
  void throwsWhenUserIsNotMember() {
    HouseholdId householdId = HouseholdId.newId();
    Household household = Household.createForOwner(householdId, ownerId).join(partnerId);
    given(householdRepository.findById(householdId)).willReturn(Optional.of(household));

    assertThatThrownBy(() -> handler.leave(new LeaveHouseholdCommand(householdId, strangerUserId)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }
}
