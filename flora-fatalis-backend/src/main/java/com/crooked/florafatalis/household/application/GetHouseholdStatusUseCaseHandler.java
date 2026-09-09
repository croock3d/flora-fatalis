package com.crooked.florafatalis.household.application;

import com.crooked.florafatalis.household.application.port.in.GetHouseholdStatusUseCase;
import com.crooked.florafatalis.household.application.port.out.HouseholdInvitationRepository;
import com.crooked.florafatalis.household.application.port.out.HouseholdRepository;
import com.crooked.florafatalis.household.application.port.out.UserActiveHouseholdPort;
import com.crooked.florafatalis.household.application.port.out.UserLookupPort;
import com.crooked.florafatalis.household.domain.Household;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.HouseholdInvitation;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetHouseholdStatusUseCaseHandler implements GetHouseholdStatusUseCase {

  private final HouseholdRepository householdRepository;
  private final HouseholdInvitationRepository invitationRepository;
  private final UserActiveHouseholdPort userActiveHouseholdPort;
  private final UserLookupPort userLookupPort;

  @Override
  public HouseholdOverview getStatus(UserId userId) {
    List<Household> memberships = householdRepository.findAllByMember(userId);
    UUID activeId =
        userActiveHouseholdPort
            .get(userId)
            .filter(
                householdId ->
                    memberships.stream().anyMatch(household -> household.id().equals(householdId)))
            .or(() -> householdRepository.findOwnedBy(userId).map(Household::id))
            .map(HouseholdId::value)
            .orElse(null);

    List<MembershipView> households =
        memberships.stream().map(household -> toMembership(userId, household, activeId)).toList();

    InvitationView incoming =
        invitationRepository.findByInvitee(userId).map(this::toIncoming).orElse(null);

    InvitationView outgoing =
        householdRepository
            .findOwnedBy(userId)
            .flatMap(owned -> invitationRepository.findByHouseholdId(owned.id()))
            .map(this::toOutgoing)
            .orElse(null);

    return new HouseholdOverview(activeId, households, incoming, outgoing);
  }

  private MembershipView toMembership(UserId currentUser, Household household, UUID activeId) {
    boolean ownedByMe = household.ownerId().equals(currentUser);
    boolean active = household.id().value().equals(activeId);
    String partnerDisplayName =
        household.hasPartner()
            ? userLookupPort.findDisplayNameById(
                ownedByMe ? household.partnerId() : household.ownerId())
            : null;
    String label =
        ownedByMe
            ? (partnerDisplayName == null ? "Moje gospodarstwo" : partnerDisplayName)
            : userLookupPort.findDisplayNameById(household.ownerId());
    return new MembershipView(
        household.id().value(),
        label,
        ownedByMe,
        active,
        household.hasPartner(),
        partnerDisplayName);
  }

  private InvitationView toIncoming(HouseholdInvitation invitation) {
    return new InvitationView(
        invitation.id().value(),
        invitation.householdId().value(),
        userLookupPort.findDisplayNameById(invitation.inviterId()),
        false);
  }

  private InvitationView toOutgoing(HouseholdInvitation invitation) {
    return new InvitationView(
        invitation.id().value(),
        invitation.householdId().value(),
        userLookupPort.findDisplayNameById(invitation.inviteeId()),
        true);
  }
}
