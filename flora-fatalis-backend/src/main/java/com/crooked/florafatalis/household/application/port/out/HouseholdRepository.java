package com.crooked.florafatalis.household.application.port.out;

import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import java.util.Optional;

public interface HouseholdRepository {

  void save(Household household);

  Optional<Household> findById(HouseholdId id);

  Optional<Household> findOwnedBy(UserId userId);

  List<Household> findAllByMember(UserId userId);
}
