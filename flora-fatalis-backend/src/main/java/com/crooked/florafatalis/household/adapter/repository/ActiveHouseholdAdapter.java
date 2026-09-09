package com.crooked.florafatalis.household.adapter.repository;

import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActiveHouseholdAdapter implements ActiveHouseholdPort {

  private final UserActiveHouseholdPort userActiveHouseholdPort;
  private final HouseholdRepository householdRepository;

  @Override
  public Optional<HouseholdId> findActiveHouseholdId(UserId userId) {
    Optional<HouseholdId> stored = userActiveHouseholdPort.get(userId);
    if (stored.isPresent()
        && householdRepository
            .findById(stored.get())
            .filter(household -> household.isMember(userId))
            .isPresent()) {
      return stored;
    }
    return householdRepository.findOwnedBy(userId).map(household -> household.id());
  }
}
