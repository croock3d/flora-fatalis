package com.crooked.florafatalis.household.domain;

import java.util.Objects;
import java.util.UUID;

public record HouseholdInvitationId(UUID value) {

  public HouseholdInvitationId {
    Objects.requireNonNull(value, "HouseholdInvitationId value must not be null");
  }

  public static HouseholdInvitationId newId() {
    return new HouseholdInvitationId(UUID.randomUUID());
  }
}
