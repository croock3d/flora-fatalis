package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.in.RejectHouseholdInvitationUseCase;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.household.domain.HouseholdInvitationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectHouseholdInvitationUseCaseHandler implements RejectHouseholdInvitationUseCase {

  private final HouseholdInvitationRepository invitationRepository;

  @Override
  @Transactional
  public void reject(RejectHouseholdInvitationCommand command) {
    HouseholdInvitation invitation =
        invitationRepository
            .findById(command.invitationId())
            .orElseThrow(HouseholdInvitationNotFoundException::new);

    if (!invitation.isParticipant(command.currentUserId())) {
      throw new HouseholdAccessDeniedException();
    }

    invitationRepository.delete(invitation.id());
  }
}
