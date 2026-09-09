package com.crooked.florafatalis.user.domain;

import java.util.Objects;

public record DisplayName(String value) {

  public DisplayName {
    Objects.requireNonNull(value, "displayName must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("displayName must not be blank");
    }
    if (value.length() < 2) {
      throw new IllegalArgumentException("displayName must have at least 2 characters");
    }
    if (value.length() > 50) {
      throw new IllegalArgumentException("displayName must have at most 50 characters");
    }
  }
}
