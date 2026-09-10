package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.in.AcceptHouseholdInvitationUseCase;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationNotFoundException;
import com.crooked.florafatalis.household.domain.HouseholdNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcceptHouseholdInvitationUseCaseHandler implements AcceptHouseholdInvitationUseCase {

  private final HouseholdInvitationRepository invitationRepository;
  private final HouseholdRepository householdRepository;
  private final UserActiveHouseholdPort userActiveHouseholdPort;

  @Override
  @Transactional
  public void accept(AcceptHouseholdInvitationCommand command) {
    HouseholdInvitation invitation =
        invitationRepository
            .findById(command.invitationId())
            .orElseThrow(HouseholdInvitationNotFoundException::new);

    if (!invitation.inviteeId().equals(command.currentUserId())) {
      throw new HouseholdAccessDeniedException();
    }

    Household inviterHousehold =
        householdRepository
            .findById(invitation.householdId())
            .orElseThrow(HouseholdNotFoundException::new);

    if (inviterHousehold.hasPartner()) {
      throw new IllegalStateException("Inviter household already has a partner");
    }
    if (inviterHousehold.isMember(invitation.inviteeId())) {
      throw new IllegalStateException("Invitee is already a member");
    }

    householdRepository.save(inviterHousehold.join(invitation.inviteeId()));
    userActiveHouseholdPort.set(invitation.inviteeId(), inviterHousehold.id());
    invitationRepository.delete(invitation.id());
  }
}
