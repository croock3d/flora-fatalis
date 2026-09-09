package com.crooked.florafatalis.user.domain;

import java.util.Objects;

public record AuthToken(String value) {

  public AuthToken {
    Objects.requireNonNull(value, "token value must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("token value must not be blank");
    }
  }
}
