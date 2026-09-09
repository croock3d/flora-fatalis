package com.crooked.florafatalis.household.domain;

import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;

public record Household(HouseholdId id, UserId ownerId, UserId partnerId, Instant createdAt) {

  public Household {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(ownerId, "ownerId must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
  }

  public static Household createForOwner(HouseholdId id, UserId ownerId) {
    return new Household(id, ownerId, null, Instant.now());
  }

  public boolean hasPartner() {
    return partnerId != null;
  }

  public boolean isMember(UserId userId) {
    return ownerId.equals(userId) || (partnerId != null && partnerId.equals(userId));
  }

  public Household join(UserId newPartnerId) {
    Objects.requireNonNull(newPartnerId, "newPartnerId must not be null");
    if (hasPartner()) {
      throw new IllegalStateException("Household already has a partner");
    }
    if (ownerId.equals(newPartnerId)) {
      throw new IllegalArgumentException("Cannot join own household");
    }
    return new Household(id, ownerId, newPartnerId, createdAt);
  }

  public Household removePartner() {
    if (!hasPartner()) {
      throw new IllegalStateException("Household has no partner");
    }
    return new Household(id, ownerId, null, createdAt);
  }

  public Household withOwner(UserId newOwnerId) {
    Objects.requireNonNull(newOwnerId, "newOwnerId must not be null");
    return new Household(id, newOwnerId, null, createdAt);
  }
}
