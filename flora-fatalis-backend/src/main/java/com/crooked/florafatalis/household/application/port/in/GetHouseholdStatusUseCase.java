package com.crooked.florafatalis.household.application.port.in;

import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import java.util.UUID;

public interface GetHouseholdStatusUseCase {

  HouseholdOverview getStatus(UserId userId);

  record HouseholdOverview(
      UUID activeHouseholdId,
      List<MembershipView> households,
      InvitationView incomingInvitation,
      InvitationView outgoingInvitation) {}

  record MembershipView(
      UUID householdId,
      String label,
      boolean ownedByMe,
      boolean active,
      boolean hasPartner,
      String partnerDisplayName) {}

  record InvitationView(
      UUID invitationId, UUID householdId, String partnerDisplayName, boolean invitedByMe) {}
}
