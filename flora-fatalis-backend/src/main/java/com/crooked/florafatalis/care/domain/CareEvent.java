package com.crooked.florafatalis.care.domain;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.Instant;
import java.util.Objects;

public record CareEvent(
    CareEventId id,
    PlantId plantId,
    HouseholdId householdId,
    CareType careType,
    Instant performedAt,
    UserId performedBy,
    Integer quantityMl,
    CareSource source,
    String notes,
    PruningKind pruningKind) {

  public CareEvent {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(plantId, "plantId must not be null");
    Objects.requireNonNull(householdId, "householdId must not be null");
    Objects.requireNonNull(careType, "careType must not be null");
    Objects.requireNonNull(performedAt, "performedAt must not be null");
    Objects.requireNonNull(performedBy, "performedBy must not be null");
    Objects.requireNonNull(source, "source must not be null");
    if (quantityMl != null && quantityMl < 1) {
      throw new IllegalArgumentException("quantityMl must be at least 1");
    }
    notes = normalizeNotes(notes);
    if (pruningKind != null && careType != CareType.PRUNING) {
      throw new IllegalArgumentException("pruningKind is only allowed for PRUNING");
    }
  }

  private static String normalizeNotes(String notes) {
    if (notes == null || notes.isBlank()) {
      return null;
    }
    String stripped = notes.strip();
    if (stripped.length() > 500) {
      throw new IllegalArgumentException("notes must have at most 500 characters");
    }
    return stripped;
  }

  public static CareEvent watering(
      PlantId plantId,
      HouseholdId householdId,
      UserId performedBy,
      Instant performedAt,
      Integer quantityMl) {
    return new CareEvent(
        CareEventId.newId(),
        plantId,
        householdId,
        CareType.WATERING,
        performedAt,
        performedBy,
        quantityMl,
        CareSource.MANUAL,
        null,
        null);
  }

  public static CareEvent fertilizing(
      PlantId plantId, HouseholdId householdId, UserId performedBy, Instant performedAt) {
    return new CareEvent(
        CareEventId.newId(),
        plantId,
        householdId,
        CareType.FERTILIZING,
        performedAt,
        performedBy,
        null,
        CareSource.MANUAL,
        null,
        null);
  }

  public static CareEvent pruning(
      PlantId plantId,
      HouseholdId householdId,
      UserId performedBy,
      Instant performedAt,
      PruningKind pruningKind,
      String notes) {
    return new CareEvent(
        CareEventId.newId(),
        plantId,
        householdId,
        CareType.PRUNING,
        performedAt,
        performedBy,
        null,
        CareSource.MANUAL,
        notes,
        pruningKind);
  }
}
