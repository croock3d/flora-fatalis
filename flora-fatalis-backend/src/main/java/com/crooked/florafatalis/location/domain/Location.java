package com.crooked.florafatalis.location.domain;

import com.crooked.florafatalis.household.domain.HouseholdId;
import java.util.Objects;

public record Location(LocationId id, HouseholdId householdId, String name, LocationKind kind) {

  public Location {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(householdId, "householdId must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(kind, "kind must not be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (name.length() > 80) {
      throw new IllegalArgumentException("name must have at most 80 characters");
    }
  }

  public static Location create(HouseholdId householdId, String name, LocationKind kind) {
    return new Location(LocationId.newId(), householdId, name.strip(), kind);
  }

  public Location update(String newName, LocationKind newKind) {
    return new Location(id, householdId, newName.strip(), newKind);
  }
}
