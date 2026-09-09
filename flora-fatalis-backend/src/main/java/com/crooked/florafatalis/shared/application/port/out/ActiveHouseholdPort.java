package com.crooked.florafatalis.shared.application.port.out;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;

public interface ActiveHouseholdPort {

  Optional<HouseholdId> findActiveHouseholdId(UserId userId);
}
