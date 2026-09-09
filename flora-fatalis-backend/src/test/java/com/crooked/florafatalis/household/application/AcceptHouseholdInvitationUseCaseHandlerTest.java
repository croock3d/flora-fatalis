package com.crooked.florafatalis.household.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.application.port.in.AcceptHouseholdInvitationUseCase.AcceptHouseholdInvitationCommand;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.household.domain.HouseholdInvitationNotFoundException;
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
class AcceptHouseholdInvitationUseCaseHandlerTest {

  @Mock HouseholdInvitationRepository invitationRepository;
  @Mock HouseholdRepository householdRepository;
  @Mock UserActiveHouseholdPort userActiveHouseholdPort;

  AcceptHouseholdInvitationUseCaseHandler handler;

  private final UserId inviterId = new UserId(UUID.randomUUID());
  private final UserId inviteeId = new UserId(UUID.randomUUID());
  private final UserId strangerUserId = new UserId(UUID.randomUUID());
  private final HouseholdId inviterHouseholdId = HouseholdId.newId();

  @BeforeEach
  void setUp() {
    handler =
        new AcceptHouseholdInvitationUseCaseHandler(
            invitationRepository, householdRepository, userActiveHouseholdPort);
  }

  @Test
  void acceptsInvitationWithoutRemovingInviteeHousehold() {
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    HouseholdInvitation invitation =
        HouseholdInvitation.create(invitationId, inviterHouseholdId, inviterId, inviteeId);
    Household inviterHousehold = Household.createForOwner(inviterHouseholdId, inviterId);
    given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));
    given(householdRepository.findById(inviterHouseholdId))
        .willReturn(Optional.of(inviterHousehold));

    handler.accept(new AcceptHouseholdInvitationCommand(invitationId, inviteeId));

    ArgumentCaptor<Household> captor = ArgumentCaptor.forClass(Household.class);
    then(householdRepository).should().save(captor.capture());
    assertThat(captor.getValue().partnerId()).isEqualTo(inviteeId);
    assertThat(captor.getValue().id()).isEqualTo(inviterHouseholdId);
    then(householdRepository).shouldHaveNoMoreInteractions();
    then(userActiveHouseholdPort).should().set(inviteeId, inviterHouseholdId);
    then(invitationRepository).should().delete(invitationId);
  }

  @Test
  void throwsWhenNoInvitationFound() {
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    given(invitationRepository.findById(invitationId)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> handler.accept(new AcceptHouseholdInvitationCommand(invitationId, inviteeId)))
        .isInstanceOf(HouseholdInvitationNotFoundException.class);
  }

  @Test
  void throwsWhenCurrentUserIsNotInvitee() {
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    HouseholdInvitation invitation =
        HouseholdInvitation.create(invitationId, inviterHouseholdId, inviterId, inviteeId);
    given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

    assertThatThrownBy(
            () ->
                handler.accept(new AcceptHouseholdInvitationCommand(invitationId, strangerUserId)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }
}
