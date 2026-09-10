package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.in.SendHouseholdInvitationUseCase;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserLookupPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdAlreadyExistsException;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationId;
import com.crooked.florafatalis.shared.domain.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SendHouseholdInvitationUseCaseHandler implements SendHouseholdInvitationUseCase {

  private final HouseholdRepository householdRepository;
  private final HouseholdInvitationRepository invitationRepository;
  private final UserLookupPort userLookupPort;

  @Override
  @Transactional
  public void send(SendHouseholdInvitationCommand command) {
    UserId inviteeId =
        userLookupPort
            .findUserIdByEmail(command.inviteeEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (command.inviterId().equals(inviteeId)) {
      throw new IllegalArgumentException("Cannot invite yourself");
    }

    Household inviterHousehold =
        householdRepository
            .findOwnedBy(command.inviterId())
            .orElseThrow(() -> new IllegalStateException("Inviter has no household"));

    if (inviterHousehold.hasPartner()) {
      throw new HouseholdAlreadyExistsException();
    }
    if (invitationRepository.findByHouseholdId(inviterHousehold.id()).isPresent()) {
      throw new HouseholdAlreadyExistsException();
    }
    if (inviterHousehold.isMember(inviteeId)) {
      throw new HouseholdAlreadyExistsException();
    }
    if (invitationRepository.findByInvitee(inviteeId).isPresent()) {
      throw new HouseholdAlreadyExistsException();
    }

    HouseholdInvitation invitation =
        HouseholdInvitation.create(
            HouseholdInvitationId.newId(), inviterHousehold.id(), command.inviterId(), inviteeId);
    invitationRepository.save(invitation);
  }
}
