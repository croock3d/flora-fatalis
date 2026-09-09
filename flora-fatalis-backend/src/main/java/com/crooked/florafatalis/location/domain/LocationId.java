package com.crooked.florafatalis.location.domain;

import java.util.Objects;
import java.util.UUID;

public record LocationId(UUID value) {

  public LocationId {
    Objects.requireNonNull(value, "LocationId value must not be null");
  }

  public static LocationId newId() {
    return new LocationId(UUID.randomUUID());
  }
}
