package com.crooked.florafatalis.user.domain;

import java.util.Objects;

public record HashedPassword(String value) {

  public HashedPassword {
    Objects.requireNonNull(value, "hashedPassword must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("hashedPassword must not be blank");
    }
  }
}
