package com.crooked.florafatalis.household.application.port.in;

import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface RejectHouseholdInvitationUseCase {

  void reject(RejectHouseholdInvitationCommand command);

  record RejectHouseholdInvitationCommand(
      HouseholdInvitationId invitationId, UserId currentUserId) {}
}
