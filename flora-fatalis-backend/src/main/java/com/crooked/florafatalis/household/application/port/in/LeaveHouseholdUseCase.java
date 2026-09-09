package com.crooked.florafatalis.household.application.port.in;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface LeaveHouseholdUseCase {

  void leave(LeaveHouseholdCommand command);

  record LeaveHouseholdCommand(HouseholdId householdId, UserId currentUserId) {}
}
