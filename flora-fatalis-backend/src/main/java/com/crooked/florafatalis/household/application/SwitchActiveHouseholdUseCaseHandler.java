package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.in.SwitchActiveHouseholdUseCase;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SwitchActiveHouseholdUseCaseHandler implements SwitchActiveHouseholdUseCase {

  private final HouseholdRepository householdRepository;
  private final UserActiveHouseholdPort userActiveHouseholdPort;

  @Override
  @Transactional
  public void switchTo(SwitchActiveHouseholdCommand command) {
    Household household =
        householdRepository
            .findById(command.householdId())
            .orElseThrow(HouseholdNotFoundException::new);

    if (!household.isMember(command.currentUserId())) {
      throw new HouseholdAccessDeniedException();
    }

    userActiveHouseholdPort.set(command.currentUserId(), household.id());
  }
}
