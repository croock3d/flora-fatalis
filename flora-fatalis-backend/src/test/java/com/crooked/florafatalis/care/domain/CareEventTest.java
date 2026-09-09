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
}
