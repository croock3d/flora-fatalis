package com.crooked.florafatalis.household.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crooked.florafatalis.shared.domain.UserId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HouseholdInvitationTest {

  @Test
  void createStoresParticipants() {
    HouseholdInvitationId id = HouseholdInvitationId.newId();
    HouseholdId householdId = HouseholdId.newId();
    UserId inviterId = new UserId(UUID.randomUUID());
    UserId inviteeId = new UserId(UUID.randomUUID());

    HouseholdInvitation invitation =
        HouseholdInvitation.create(id, householdId, inviterId, inviteeId);

    assertThat(invitation.id()).isEqualTo(id);
    assertThat(invitation.householdId()).isEqualTo(householdId);
    assertThat(invitation.inviterId()).isEqualTo(inviterId);
    assertThat(invitation.inviteeId()).isEqualTo(inviteeId);
    assertThat(invitation.createdAt()).isNotNull();
    assertThat(invitation.isParticipant(inviterId)).isTrue();
    assertThat(invitation.isParticipant(inviteeId)).isTrue();
  }

  @Test
  void cannotInviteYourself() {
    UserId userId = new UserId(UUID.randomUUID());

    assertThatThrownBy(
            () ->
                HouseholdInvitation.create(
                    HouseholdInvitationId.newId(), HouseholdId.newId(), userId, userId))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
