package com.crooked.florafatalis.household.application.port.out;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;

public interface UserActiveHouseholdPort {

  Optional<HouseholdId> get(UserId userId);

  void set(UserId userId, HouseholdId householdId);
}
