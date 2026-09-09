package com.crooked.florafatalis.household.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.household.application.port.in.GetHouseholdStatusUseCase.HouseholdOverview;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.application.port.out.UserLookupPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetHouseholdStatusUseCaseHandlerTest {

  @Mock HouseholdRepository householdRepository;
  @Mock HouseholdInvitationRepository invitationRepository;
  @Mock UserActiveHouseholdPort userActiveHouseholdPort;
  @Mock UserLookupPort userLookupPort;

  GetHouseholdStatusUseCaseHandler handler;

  private final UserId userId = new UserId(UUID.randomUUID());
  private final UserId partnerId = new UserId(UUID.randomUUID());

  @BeforeEach
  void setUp() {
    handler =
        new GetHouseholdStatusUseCaseHandler(
            householdRepository, invitationRepository, userActiveHouseholdPort, userLookupPort);
  }

  @Test
  void returnsIncomingInvitation() {
    HouseholdId householdId = HouseholdId.newId();
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    Household owned = Household.createForOwner(HouseholdId.newId(), userId);
    HouseholdInvitation invitation =
        HouseholdInvitation.create(invitationId, householdId, partnerId, userId);
    given(householdRepository.findAllByMember(userId)).willReturn(List.of(owned));
    given(userActiveHouseholdPort.get(userId)).willReturn(Optional.of(owned.id()));
    given(invitationRepository.findByInvitee(userId)).willReturn(Optional.of(invitation));
    given(householdRepository.findOwnedBy(userId)).willReturn(Optional.of(owned));
    given(invitationRepository.findByHouseholdId(owned.id())).willReturn(Optional.empty());
    given(userLookupPort.findDisplayNameById(partnerId)).willReturn("Inviter Name");

    HouseholdOverview result = handler.getStatus(userId);

    assertThat(result.incomingInvitation()).isNotNull();
    assertThat(result.incomingInvitation().invitedByMe()).isFalse();
    assertThat(result.incomingInvitation().partnerDisplayName()).isEqualTo("Inviter Name");
    assertThat(result.incomingInvitation().invitationId()).isEqualTo(invitationId.value());
  }

  @Test
  void returnsOutgoingInvitation() {
    HouseholdId householdId = HouseholdId.newId();
    HouseholdInvitationId invitationId = HouseholdInvitationId.newId();
    Household household = Household.createForOwner(householdId, userId);
    HouseholdInvitation invitation =
        HouseholdInvitation.create(invitationId, householdId, userId, partnerId);
    given(householdRepository.findAllByMember(userId)).willReturn(List.of(household));
    given(userActiveHouseholdPort.get(userId)).willReturn(Optional.of(householdId));
    given(invitationRepository.findByInvitee(userId)).willReturn(Optional.empty());
    given(householdRepository.findOwnedBy(userId)).willReturn(Optional.of(household));
    given(invitationRepository.findByHouseholdId(householdId)).willReturn(Optional.of(invitation));
    given(userLookupPort.findDisplayNameById(partnerId)).willReturn("Partner Name");

    HouseholdOverview result = handler.getStatus(userId);

    assertThat(result.outgoingInvitation()).isNotNull();
    assertThat(result.outgoingInvitation().invitedByMe()).isTrue();
    assertThat(result.outgoingInvitation().partnerDisplayName()).isEqualTo("Partner Name");
  }

  @Test
  void returnsMembershipsAndActiveHousehold() {
    HouseholdId ownedId = HouseholdId.newId();
    HouseholdId sharedId = HouseholdId.newId();
    Household owned = Household.createForOwner(ownedId, userId);
    Household shared = Household.createForOwner(sharedId, partnerId).join(userId);
    given(householdRepository.findAllByMember(userId)).willReturn(List.of(owned, shared));
    given(userActiveHouseholdPort.get(userId)).willReturn(Optional.of(sharedId));
    given(invitationRepository.findByInvitee(userId)).willReturn(Optional.empty());
    given(householdRepository.findOwnedBy(userId)).willReturn(Optional.of(owned));
    given(invitationRepository.findByHouseholdId(ownedId)).willReturn(Optional.empty());
    given(userLookupPort.findDisplayNameById(partnerId)).willReturn("Partner");

    HouseholdOverview result = handler.getStatus(userId);

    assertThat(result.activeHouseholdId()).isEqualTo(sharedId.value());
    assertThat(result.households()).hasSize(2);
    assertThat(result.households().get(1).active()).isTrue();
    assertThat(result.households().get(1).ownedByMe()).isFalse();
  }
}
