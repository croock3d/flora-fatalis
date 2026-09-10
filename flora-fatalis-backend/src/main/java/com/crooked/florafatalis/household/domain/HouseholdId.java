package com.crooked.florafatalis.household.domain;

import java.util.Objects;
import java.util.UUID;

public record HouseholdId(UUID value) {

  public HouseholdId {
    Objects.requireNonNull(value, "HouseholdId value must not be null");
  }

  public static HouseholdId newId() {
    return new HouseholdId(UUID.randomUUID());
  }
}
