package com.crooked.florafatalis.location.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crooked.florafatalis.household.domain.HouseholdId;
import org.junit.jupiter.api.Test;

class LocationTest {

  private final HouseholdId householdId = HouseholdId.newId();

  @Test
  void createStripsName() {
    Location location = Location.create(householdId, "  Salon  ", LocationKind.INDOOR);

    assertThat(location.name()).isEqualTo("Salon");
    assertThat(location.kind()).isEqualTo(LocationKind.INDOOR);
    assertThat(location.householdId()).isEqualTo(householdId);
    assertThat(location.id()).isNotNull();
  }

  @Test
  void blankNameThrows() {
    assertThatThrownBy(() -> Location.create(householdId, "  ", LocationKind.INDOOR))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void renameUpdatesNameAndKind() {
    Location location = Location.create(householdId, "Salon", LocationKind.INDOOR);

    Location updated = location.rename("Balkon", LocationKind.BALCONY);

    assertThat(updated.id()).isEqualTo(location.id());
    assertThat(updated.name()).isEqualTo("Balkon");
    assertThat(updated.kind()).isEqualTo(LocationKind.BALCONY);
  }
}
