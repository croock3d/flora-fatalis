package com.crooked.florafatalis.household.domain;

import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;

public record HouseholdInvitation(
    HouseholdInvitationId id,
    HouseholdId householdId,
    UserId inviterId,
    UserId inviteeId,
    Instant createdAt) {

  public HouseholdInvitation {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(householdId, "householdId must not be null");
    Objects.requireNonNull(inviterId, "inviterId must not be null");
    Objects.requireNonNull(inviteeId, "inviteeId must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    if (inviterId.equals(inviteeId)) {
      throw new IllegalArgumentException("Cannot invite yourself");
    }
  }

  public static HouseholdInvitation create(
      HouseholdInvitationId id, HouseholdId householdId, UserId inviterId, UserId inviteeId) {
    return new HouseholdInvitation(id, householdId, inviterId, inviteeId, Instant.now());
  }

  public boolean isParticipant(UserId userId) {
    return inviterId.equals(userId) || inviteeId.equals(userId);
  }
}
