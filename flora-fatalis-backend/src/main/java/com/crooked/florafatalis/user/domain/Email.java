package com.crooked.florafatalis.user.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  public Email {
    Objects.requireNonNull(value, "email must not be null");
    String normalized = value.toLowerCase().strip();
    if (normalized.isBlank()) {
      throw new IllegalArgumentException("email must not be blank");
    }
    if (!EMAIL_PATTERN.matcher(normalized).matches()) {
      throw new IllegalArgumentException("email is invalid: " + value);
    }
    value = normalized;
  }
}
