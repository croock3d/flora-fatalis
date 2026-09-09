package com.crooked.florafatalis.plant.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class PlantTest {

  private final HouseholdId householdId = HouseholdId.newId();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final LocationId locationId = LocationId.newId();
  private final UserId userId = UserId.newId();
  private final Instant now = Instant.parse("2026-01-01T00:00:00Z");

  @Test
  void createStripsName() {
    Plant plant =
        Plant.create(householdId, speciesId, locationId, "  Monstera  ", 10, now, userId, now);

    assertThat(plant.name()).isEqualTo("Monstera");
    assertThat(plant.wateringIntervalDaysOverride()).isEqualTo(10);
    assertThat(plant.isArchived()).isFalse();
  }

  @Test
  void blankNameThrows() {
    assertThatThrownBy(
            () -> Plant.create(householdId, speciesId, locationId, "  ", null, null, userId, now))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void archiveSetsArchivedAt() {
    Plant plant =
        Plant.create(householdId, speciesId, locationId, "Monstera", null, null, userId, now);

    Plant archived = plant.archive(now);

    assertThat(archived.isArchived()).isTrue();
    assertThat(archived.archivedAt()).isEqualTo(now);
    assertThat(archived.id()).isEqualTo(plant.id());
  }

  @Test
  void archiveTwiceThrows() {
    Plant archived =
        Plant.create(householdId, speciesId, locationId, "Monstera", null, null, userId, now)
            .archive(now);

    assertThatThrownBy(() -> archived.archive(now)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void updateArchivedThrows() {
    Plant archived =
        Plant.create(householdId, speciesId, locationId, "Monstera", null, null, userId, now)
            .archive(now);

    assertThatThrownBy(() -> archived.update(speciesId, locationId, "Inna", null, null))
        .isInstanceOf(IllegalStateException.class);
  }
}
