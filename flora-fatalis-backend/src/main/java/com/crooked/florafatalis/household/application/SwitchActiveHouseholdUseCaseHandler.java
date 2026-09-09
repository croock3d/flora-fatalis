package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.in.SwitchActiveHouseholdUseCase;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdInvitationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SwitchActiveHouseholdUseCaseHandler implements SwitchActiveHouseholdUseCase {

  private final HouseholdRepository householdRepository;
  private final UserActiveHouseholdPort userActiveHouseholdPort;

  @Override
  public void switchTo(SwitchActiveHouseholdCommand command) {
    Household household =
        householdRepository
            .findById(command.householdId())
            .orElseThrow(HouseholdInvitationNotFoundException::new);

    if (!household.isMember(command.currentUserId())) {
      throw new HouseholdAccessDeniedException();
    }

    userActiveHouseholdPort.set(command.currentUserId(), household.id());
  }
}
