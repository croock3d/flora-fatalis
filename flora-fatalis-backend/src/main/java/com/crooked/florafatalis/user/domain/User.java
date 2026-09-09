package com.crooked.florafatalis.user.domain;

import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;

public class User {

  private final UserId id;
  private final Email email;
  private final HashedPassword hashedPassword;
  private final DisplayName displayName;
  private final Instant createdAt;

  private User(
      UserId id,
      Email email,
      HashedPassword hashedPassword,
      DisplayName displayName,
      Instant createdAt) {
    this.id = id;
    this.email = email;
    this.hashedPassword = hashedPassword;
    this.displayName = displayName;
    this.createdAt = createdAt;
  }

  public static User register(
      UserId id, Email email, HashedPassword hashedPassword, DisplayName displayName) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(email, "email must not be null");
    Objects.requireNonNull(hashedPassword, "hashedPassword must not be null");
    Objects.requireNonNull(displayName, "displayName must not be null");
    return new User(id, email, hashedPassword, displayName, Instant.now());
  }

  public UserId id() {
    return id;
  }

  public Email email() {
    return email;
  }

  public HashedPassword hashedPassword() {
    return hashedPassword;
  }

  public DisplayName displayName() {
    return displayName;
  }

  public Instant createdAt() {
    return createdAt;
  }
}
