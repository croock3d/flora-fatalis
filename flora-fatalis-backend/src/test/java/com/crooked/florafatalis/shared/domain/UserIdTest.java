package com.crooked.florafatalis.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserIdTest {

  @Test
  void sameUuidProduceEqualUserIds() {
    // given
    UUID uuid = UUID.randomUUID();

    // when
    UserId id1 = new UserId(uuid);
    UserId id2 = new UserId(uuid);

    // then
    assertThat(id1).isEqualTo(id2);
  }

  @Test
  void differentNewIdsAreNotEqual() {
    // when
    UserId id1 = UserId.newId();
    UserId id2 = UserId.newId();

    // then
    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void nullUuidThrowsException() {
    assertThatThrownBy(() -> new UserId(null)).isInstanceOf(NullPointerException.class);
  }
}
