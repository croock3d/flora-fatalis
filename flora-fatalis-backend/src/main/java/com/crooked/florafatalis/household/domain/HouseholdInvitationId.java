package com.crooked.florafatalis.household.domain;

import java.util.UUID;

public record HouseholdInvitationId(UUID value) {

  public static HouseholdInvitationId newId() {
    return new HouseholdInvitationId(UUID.randomUUID());
  }
}
