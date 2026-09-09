package com.crooked.florafatalis.household.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crooked.florafatalis.shared.domain.UserId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HouseholdTest {

  private final HouseholdId householdId = new HouseholdId(UUID.randomUUID());
  private final UserId ownerId = new UserId(UUID.randomUUID());
  private final UserId partnerId = new UserId(UUID.randomUUID());

  @Test
  void createForOwnerStartsWithoutPartner() {
    Household household = Household.createForOwner(householdId, ownerId);

    assertThat(household.id()).isEqualTo(householdId);
    assertThat(household.ownerId()).isEqualTo(ownerId);
    assertThat(household.partnerId()).isNull();
    assertThat(household.hasPartner()).isFalse();
    assertThat(household.createdAt()).isNotNull();
  }

  @Test
  void joinAddsPartner() {
    Household household = Household.createForOwner(householdId, ownerId).join(partnerId);

    assertThat(household.hasPartner()).isTrue();
    assertThat(household.partnerId()).isEqualTo(partnerId);
    assertThat(household.ownerId()).isEqualTo(ownerId);
  }

  @Test
  void joinWhenAlreadyHasPartnerThrows() {
    Household household = Household.createForOwner(householdId, ownerId).join(partnerId);

    assertThatThrownBy(() -> household.join(new UserId(UUID.randomUUID())))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void joinOwnHouseholdThrows() {
    Household household = Household.createForOwner(householdId, ownerId);

    assertThatThrownBy(() -> household.join(ownerId)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void removePartnerClearsPartner() {
    Household household =
        Household.createForOwner(householdId, ownerId).join(partnerId).removePartner();

    assertThat(household.hasPartner()).isFalse();
    assertThat(household.partnerId()).isNull();
    assertThat(household.ownerId()).isEqualTo(ownerId);
  }

  @Test
  void withOwnerChangesOwnerAndClearsPartner() {
    Household household =
        Household.createForOwner(householdId, ownerId).join(partnerId).withOwner(partnerId);

    assertThat(household.ownerId()).isEqualTo(partnerId);
    assertThat(household.partnerId()).isNull();
  }

  @Test
  void isMemberRecognizesOwnerAndPartner() {
    Household household = Household.createForOwner(householdId, ownerId).join(partnerId);

    assertThat(household.isMember(ownerId)).isTrue();
    assertThat(household.isMember(partnerId)).isTrue();
    assertThat(household.isMember(new UserId(UUID.randomUUID()))).isFalse();
  }
}
