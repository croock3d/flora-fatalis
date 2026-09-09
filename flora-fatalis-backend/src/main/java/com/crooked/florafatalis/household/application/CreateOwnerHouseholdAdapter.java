package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.user.application.port.out.CreateOwnerHouseholdPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateOwnerHouseholdAdapter implements CreateOwnerHouseholdPort {

  private final HouseholdRepository householdRepository;
  private final UserActiveHouseholdPort userActiveHouseholdPort;

  @Override
  public void createFor(UserId userId) {
    Household household = Household.createForOwner(HouseholdId.newId(), userId);
    householdRepository.save(household);
    userActiveHouseholdPort.set(userId, household.id());
  }
}
