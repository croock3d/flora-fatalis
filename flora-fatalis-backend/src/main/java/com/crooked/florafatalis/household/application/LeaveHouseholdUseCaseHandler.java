package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.in.LeaveHouseholdUseCase;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
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
public class LeaveHouseholdUseCaseHandler implements LeaveHouseholdUseCase {

  private final HouseholdRepository householdRepository;
  private final HouseholdInvitationRepository invitationRepository;
  private final UserActiveHouseholdPort userActiveHouseholdPort;

  @Override
  @Transactional
  public void leave(LeaveHouseholdCommand command) {
    Household household =
        householdRepository
            .findById(command.householdId())
            .orElseThrow(HouseholdNotFoundException::new);

    if (!household.isMember(command.currentUserId())) {
      throw new HouseholdAccessDeniedException();
    }
    if (household.ownerId().equals(command.currentUserId())) {
      throw new IllegalStateException("Cannot leave owned household");
    }

    invitationRepository
        .findByHouseholdId(household.id())
        .ifPresent(invitation -> invitationRepository.delete(invitation.id()));

    householdRepository.save(household.removePartner());

    boolean wasActive =
        userActiveHouseholdPort
            .get(command.currentUserId())
            .filter(id -> id.equals(household.id()))
            .isPresent();
    if (wasActive) {
      householdRepository
          .findOwnedBy(command.currentUserId())
          .ifPresent(owned -> userActiveHouseholdPort.set(command.currentUserId(), owned.id()));
    }
  }
}
