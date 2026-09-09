package com.crooked.florafatalis.photo.domain;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.plant.domain.PlantId;
import java.time.Instant;
import java.util.Objects;

public record PlantPhoto(
    PlantPhotoId id,
    PlantId plantId,
    HouseholdId householdId,
    String storageKey,
    String contentType,
    Instant takenAt,
    boolean primary,
    int sortOrder) {

  public PlantPhoto {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(plantId, "plantId must not be null");
    Objects.requireNonNull(householdId, "householdId must not be null");
    Objects.requireNonNull(storageKey, "storageKey must not be null");
    Objects.requireNonNull(contentType, "contentType must not be null");
    Objects.requireNonNull(takenAt, "takenAt must not be null");
  }

  public static PlantPhoto create(
      PlantId plantId,
      HouseholdId householdId,
      String storageKey,
      String contentType,
      Instant takenAt,
      boolean primary,
      int sortOrder) {
    return new PlantPhoto(
        PlantPhotoId.newId(),
        plantId,
        householdId,
        storageKey,
        contentType,
        takenAt,
        primary,
        sortOrder);
  }

  public PlantPhoto asPrimary() {
    return new PlantPhoto(
        id, plantId, householdId, storageKey, contentType, takenAt, true, sortOrder);
  }

  public PlantPhoto asSecondary() {
    return new PlantPhoto(
        id, plantId, householdId, storageKey, contentType, takenAt, false, sortOrder);
  }
}
