package com.crooked.florafatalis.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserTest {

  private final UserId userId = UserId.newId();
  private final Email email = new Email("jan@example.com");
  private final HashedPassword hashedPassword = new HashedPassword("$2a$bcrypt_hash_value");
  private final DisplayName displayName = new DisplayName("Jan Kowalski");

  @Test
  void registerCreatesUser() {
    // when
    User user = User.register(userId, email, hashedPassword, displayName);

    // then
    assertThat(user.id()).isEqualTo(userId);
    assertThat(user.email()).isEqualTo(email);
    assertThat(user.hashedPassword()).isEqualTo(hashedPassword);
    assertThat(user.displayName()).isEqualTo(displayName);
    assertThat(user.createdAt()).isNotNull();
  }

  @Test
  void createdAtIsSetToNow() {
    // given
    Instant before = Instant.now();

    // when
    User user = User.register(userId, email, hashedPassword, displayName);

    // then
    Instant after = Instant.now();
    assertThat(user.createdAt()).isAfterOrEqualTo(before).isBeforeOrEqualTo(after);
  }

  @Test
  void reconstituteKeepsCreatedAt() {
    Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

    User user = User.reconstitute(userId, email, hashedPassword, displayName, createdAt);

    assertThat(user.createdAt()).isEqualTo(createdAt);
    assertThat(user.email()).isEqualTo(email);
  }

  @Test
  void registerWithNullUserIdThrows() {
    assertThatThrownBy(() -> User.register(null, email, hashedPassword, displayName))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void registerWithNullEmailThrows() {
    assertThatThrownBy(() -> User.register(userId, null, hashedPassword, displayName))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void registerWithNullHashedPasswordThrows() {
    assertThatThrownBy(() -> User.register(userId, email, null, displayName))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void registerWithNullDisplayNameThrows() {
    assertThatThrownBy(() -> User.register(userId, email, hashedPassword, null))
        .isInstanceOf(NullPointerException.class);
  }
}
