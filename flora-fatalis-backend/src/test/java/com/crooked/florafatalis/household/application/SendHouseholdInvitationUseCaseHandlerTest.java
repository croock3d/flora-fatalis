package com.crooked.florafatalis.household.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.crooked.florafatalis.household.application.port.in.SendHouseholdInvitationUseCase.SendHouseholdInvitationCommand;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserLookupPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdAlreadyExistsException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
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
class SendHouseholdInvitationUseCaseHandlerTest {

  @Mock HouseholdRepository householdRepository;
  @Mock HouseholdInvitationRepository invitationRepository;
  @Mock UserLookupPort userLookupPort;

  SendHouseholdInvitationUseCaseHandler handler;

  private final UserId inviterId = new UserId(UUID.randomUUID());
  private final UserId inviteeId = new UserId(UUID.randomUUID());
  private final String inviteeEmail = "invitee@example.com";
  private final Household inviterHousehold =
      Household.createForOwner(HouseholdId.newId(), inviterId);

  @BeforeEach
  void setUp() {
    handler =
        new SendHouseholdInvitationUseCaseHandler(
            householdRepository, invitationRepository, userLookupPort);
  }

  @Test
  void sendsInvitationSuccessfully() {
    given(userLookupPort.findUserIdByEmail(inviteeEmail)).willReturn(Optional.of(inviteeId));
    given(householdRepository.findOwnedBy(inviterId)).willReturn(Optional.of(inviterHousehold));
    given(invitationRepository.findByHouseholdId(inviterHousehold.id()))
        .willReturn(Optional.empty());
    given(invitationRepository.findByInvitee(inviteeId)).willReturn(Optional.empty());

    handler.send(new SendHouseholdInvitationCommand(inviterId, inviteeEmail));

    ArgumentCaptor<HouseholdInvitation> captor = ArgumentCaptor.forClass(HouseholdInvitation.class);
    then(invitationRepository).should().save(captor.capture());
    HouseholdInvitation saved = captor.getValue();
    assertThat(saved.inviterId()).isEqualTo(inviterId);
    assertThat(saved.inviteeId()).isEqualTo(inviteeId);
    assertThat(saved.householdId()).isEqualTo(inviterHousehold.id());
  }

  @Test
  void throwsWhenInviteeNotFound() {
    given(userLookupPort.findUserIdByEmail(inviteeEmail)).willReturn(Optional.empty());

    assertThatThrownBy(
            () -> handler.send(new SendHouseholdInvitationCommand(inviterId, inviteeEmail)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void throwsWhenInviterInvitesThemself() {
    given(userLookupPort.findUserIdByEmail(inviteeEmail)).willReturn(Optional.of(inviterId));

    assertThatThrownBy(
            () -> handler.send(new SendHouseholdInvitationCommand(inviterId, inviteeEmail)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void throwsWhenInviterAlreadyHasPartner() {
    Household withPartner = inviterHousehold.join(new UserId(UUID.randomUUID()));
    given(userLookupPort.findUserIdByEmail(inviteeEmail)).willReturn(Optional.of(inviteeId));
    given(householdRepository.findOwnedBy(inviterId)).willReturn(Optional.of(withPartner));

    assertThatThrownBy(
            () -> handler.send(new SendHouseholdInvitationCommand(inviterId, inviteeEmail)))
        .isInstanceOf(HouseholdAlreadyExistsException.class);
  }
}
