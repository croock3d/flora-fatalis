package com.crooked.florafatalis.household.domain;

import java.util.UUID;

public record HouseholdId(UUID value) {

  public static HouseholdId newId() {
    return new HouseholdId(UUID.randomUUID());
  }
}
