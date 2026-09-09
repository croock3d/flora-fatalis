package com.crooked.florafatalis.plant.domain;

import java.util.Objects;
import java.util.UUID;

public record PlantId(UUID value) {

  public PlantId {
    Objects.requireNonNull(value, "PlantId value must not be null");
  }

  public static PlantId newId() {
    return new PlantId(UUID.randomUUID());
  }
}
