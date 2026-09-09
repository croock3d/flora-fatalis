package com.crooked.florafatalis.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DisplayNameTest {

  @Test
  void validDisplayNameCreatesValueObject() {
    // when
    DisplayName displayName = new DisplayName("Jan Kowalski");

    // then
    assertThat(displayName.value()).isEqualTo("Jan Kowalski");
  }

  @Test
  void twoCharacterDisplayNameIsValid() {
    // when
    DisplayName displayName = new DisplayName("AB");

    // then
    assertThat(displayName.value()).isEqualTo("AB");
  }

  @Test
  void fiftyCharacterDisplayNameIsValid() {
    // given
    String fiftyChars = "A".repeat(50);

    // when
    DisplayName displayName = new DisplayName(fiftyChars);

    // then
    assertThat(displayName.value()).isEqualTo(fiftyChars);
  }

  @Test
  void nullDisplayNameThrows() {
    assertThatThrownBy(() -> new DisplayName(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void blankDisplayNameThrows() {
    assertThatThrownBy(() -> new DisplayName("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("displayName");
  }

  @Test
  void oneCharacterDisplayNameThrows() {
    assertThatThrownBy(() -> new DisplayName("A"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("displayName");
  }

  @Test
  void fiftyOneCharacterDisplayNameThrows() {
    // given
    String fiftyOneChars = "A".repeat(51);

    assertThatThrownBy(() -> new DisplayName(fiftyOneChars))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("displayName");
  }
}
