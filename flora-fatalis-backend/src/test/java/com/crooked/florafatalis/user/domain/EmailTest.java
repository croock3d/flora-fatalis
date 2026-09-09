package com.crooked.florafatalis.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  void validEmailCreatesValueObject() {
    // when
    Email email = new Email("user@example.com");

    // then
    assertThat(email.value()).isEqualTo("user@example.com");
  }

  @Test
  void emailIsNormalizedToLowercase() {
    // when
    Email email = new Email("User@Example.COM");

    // then
    assertThat(email.value()).isEqualTo("user@example.com");
  }

  @Test
  void nullEmailThrows() {
    assertThatThrownBy(() -> new Email(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void blankEmailThrows() {
    assertThatThrownBy(() -> new Email("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email");
  }

  @Test
  void emailWithoutAtSignThrows() {
    assertThatThrownBy(() -> new Email("invalidemail.com"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email");
  }

  @Test
  void emailWithoutDomainThrows() {
    assertThatThrownBy(() -> new Email("user@"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email");
  }

  @Test
  void emailWithoutLocalPartThrows() {
    assertThatThrownBy(() -> new Email("@example.com"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("email");
  }
}
