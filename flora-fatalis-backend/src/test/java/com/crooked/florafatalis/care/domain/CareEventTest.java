package com.crooked.florafatalis.care.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CareEventTest {

  @Test
  void wateringAllowsEmptyQuantity() {
    CareEvent event =
        CareEvent.watering(
            PlantId.newId(),
            HouseholdId.newId(),
            UserId.newId(),
            Instant.parse("2026-01-10T08:00:00Z"),
            null);

    assertThat(event.careType()).isEqualTo(CareType.WATERING);
    assertThat(event.quantityMl()).isNull();
    assertThat(event.source()).isEqualTo(CareSource.MANUAL);
    assertThat(event.notes()).isNull();
    assertThat(event.pruningKind()).isNull();
  }

  @Test
  void wateringRejectsNonPositiveQuantity() {
    assertThatThrownBy(
            () ->
                CareEvent.watering(
                    PlantId.newId(),
                    HouseholdId.newId(),
                    UserId.newId(),
                    Instant.parse("2026-01-10T08:00:00Z"),
                    0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("quantityMl");
  }

  @Test
  void pruningStoresOptionalKindAndNotes() {
    CareEvent event =
        CareEvent.pruning(
            PlantId.newId(),
            HouseholdId.newId(),
            UserId.newId(),
            Instant.parse("2026-01-10T08:00:00Z"),
            PruningKind.SHAPING,
            "  skrócone pędy  ");

    assertThat(event.careType()).isEqualTo(CareType.PRUNING);
    assertThat(event.pruningKind()).isEqualTo(PruningKind.SHAPING);
    assertThat(event.notes()).isEqualTo("skrócone pędy");
    assertThat(event.quantityMl()).isNull();
    assertThat(event.source()).isEqualTo(CareSource.MANUAL);
  }

  @Test
  void pruningAllowsMissingKindAndNotes() {
    CareEvent event =
        CareEvent.pruning(
            PlantId.newId(),
            HouseholdId.newId(),
            UserId.newId(),
            Instant.parse("2026-01-10T08:00:00Z"),
            null,
            "   ");

    assertThat(event.pruningKind()).isNull();
    assertThat(event.notes()).isNull();
  }

  @Test
  void pruningRejectsNotesLongerThan500() {
    assertThatThrownBy(
            () ->
                CareEvent.pruning(
                    PlantId.newId(),
                    HouseholdId.newId(),
                    UserId.newId(),
                    Instant.parse("2026-01-10T08:00:00Z"),
                    PruningKind.OTHER,
                    "x".repeat(501)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("notes");
  }
}
