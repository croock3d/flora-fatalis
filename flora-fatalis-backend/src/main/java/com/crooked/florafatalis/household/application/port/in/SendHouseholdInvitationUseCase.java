package com.crooked.florafatalis.household.application.port.in;

import com.crooked.florafatalis.shared.domain.UserId;

public interface SendHouseholdInvitationUseCase {

  void send(SendHouseholdInvitationCommand command);

  record SendHouseholdInvitationCommand(UserId inviterId, String inviteeEmail) {}
}
