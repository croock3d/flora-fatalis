package com.crooked.florafatalis.household.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.application.port.in.RejectHouseholdInvitationUseCase.RejectHouseholdInvitationCommand;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RejectHouseholdInvitationUseCaseHandlerTest {

  @Mock HouseholdInvitationRepository invitationRepository;

  RejectHouseholdInvitationUseCaseHandler handler;

  private final UserId inviterId = new UserId(UUID.randomUUID());
  private final UserId inviteeId = new UserId(UUID.randomUUID());
  private final UserId strangerUserId = new UserId(UUID.randomUUID());

  @BeforeEach
  void setUp() {
    handler = new RejectHouseholdInvitationUseCaseHandler(invitationRepository);
  }

  @Test
  void rejectsInvitationSuccessfully() {
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    HouseholdInvitation invitation =
        HouseholdInvitation.create(invitationId, HouseholdId.newId(), inviterId, inviteeId);
    given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

    handler.reject(new RejectHouseholdInvitationCommand(invitationId, inviteeId));

    then(invitationRepository).should().delete(invitation.id());
  }

  @Test
  void throwsWhenNoInvitationFound() {
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    given(invitationRepository.findById(invitationId)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> handler.reject(new RejectHouseholdInvitationCommand(invitationId, inviteeId)))
        .isInstanceOf(HouseholdInvitationNotFoundException.class);
  }

  @Test
  void inviterCanCancelInvitation() {
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    HouseholdInvitation invitation =
        HouseholdInvitation.create(invitationId, HouseholdId.newId(), inviterId, inviteeId);
    given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

    handler.reject(new RejectHouseholdInvitationCommand(invitationId, inviterId));

    then(invitationRepository).should().delete(invitation.id());
  }

  @Test
  void throwsWhenCurrentUserIsNotInvitee() {
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    HouseholdInvitation invitation =
        HouseholdInvitation.create(invitationId, HouseholdId.newId(), inviterId, inviteeId);
    given(invitationRepository.findById(invitationId)).willReturn(Optional.of(invitation));

    assertThatThrownBy(
            () ->
                handler.reject(new RejectHouseholdInvitationCommand(invitationId, strangerUserId)))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }
}
