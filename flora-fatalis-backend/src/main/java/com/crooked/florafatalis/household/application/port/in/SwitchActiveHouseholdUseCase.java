package com.crooked.florafatalis.household.application.port.in;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface SwitchActiveHouseholdUseCase {

  void switchTo(SwitchActiveHouseholdCommand command);

  record SwitchActiveHouseholdCommand(HouseholdId householdId, UserId currentUserId) {}
}
