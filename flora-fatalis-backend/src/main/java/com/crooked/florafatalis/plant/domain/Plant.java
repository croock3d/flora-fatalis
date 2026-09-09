package com.crooked.florafatalis.plant.domain;

import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import java.util.Objects;

public record Plant(
    PlantId id,
    HouseholdId householdId,
    SpeciesId speciesId,
    LocationId locationId,
    String name,
    Integer wateringIntervalDaysOverride,
    Integer fertilizingIntervalDaysOverride,
    Instant acquiredAt,
    Instant archivedAt,
    UserId createdBy,
    Instant createdAt) {

  public Plant {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(householdId, "householdId must not be null");
    Objects.requireNonNull(speciesId, "speciesId must not be null");
    Objects.requireNonNull(locationId, "locationId must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(createdBy, "createdBy must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (name.length() > 80) {
      throw new IllegalArgumentException("name must have at most 80 characters");
    }
    if (wateringIntervalDaysOverride != null && wateringIntervalDaysOverride < 1) {
      throw new IllegalArgumentException("wateringIntervalDaysOverride must be at least 1");
    }
    if (fertilizingIntervalDaysOverride != null && fertilizingIntervalDaysOverride < 1) {
      throw new IllegalArgumentException("fertilizingIntervalDaysOverride must be at least 1");
    }
  }

  public static Plant create(
      HouseholdId householdId,
      SpeciesId speciesId,
      LocationId locationId,
      String name,
      Integer wateringIntervalDaysOverride,
      Integer fertilizingIntervalDaysOverride,
      Instant acquiredAt,
      UserId createdBy,
      Instant createdAt) {
    return new Plant(
        PlantId.newId(),
        householdId,
        speciesId,
        locationId,
        name.strip(),
        wateringIntervalDaysOverride,
        fertilizingIntervalDaysOverride,
        acquiredAt,
        null,
        createdBy,
        createdAt);
  }

  public Plant update(
      SpeciesId speciesId,
      LocationId locationId,
      String name,
      Integer wateringIntervalDaysOverride,
      Integer fertilizingIntervalDaysOverride,
      Instant acquiredAt) {
    if (archivedAt != null) {
      throw new IllegalStateException("Cannot update archived plant");
    }
    return new Plant(
        id,
        householdId,
        speciesId,
        locationId,
        name.strip(),
        wateringIntervalDaysOverride,
        fertilizingIntervalDaysOverride,
        acquiredAt,
        archivedAt,
        createdBy,
        createdAt);
  }

  public Plant archive(Instant now) {
    if (archivedAt != null) {
      throw new IllegalStateException("Plant is already archived");
    }
    return new Plant(
        id,
        householdId,
        speciesId,
        locationId,
        name,
        wateringIntervalDaysOverride,
        fertilizingIntervalDaysOverride,
        acquiredAt,
        now,
        createdBy,
        createdAt);
  }

  public boolean isArchived() {
    return archivedAt != null;
  }
}
