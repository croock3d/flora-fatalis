package com.crooked.florafatalis.household.application.port.in;

import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface AcceptHouseholdInvitationUseCase {

  void accept(AcceptHouseholdInvitationCommand command);

  record AcceptHouseholdInvitationCommand(
      HouseholdInvitationId invitationId, UserId currentUserId) {}
}
