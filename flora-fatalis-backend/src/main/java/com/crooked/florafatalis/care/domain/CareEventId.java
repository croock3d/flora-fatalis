package com.crooked.florafatalis.care.domain;

import java.util.Objects;
import java.util.UUID;

public record CareEventId(UUID value) {

  public CareEventId {
    Objects.requireNonNull(value, "CareEventId value must not be null");
  }

  public static CareEventId newId() {
    return new CareEventId(UUID.randomUUID());
  }
}
