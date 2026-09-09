package com.crooked.florafatalis.species.domain;

import java.util.Objects;
import java.util.UUID;

public record SpeciesId(UUID value) {

  public SpeciesId {
    Objects.requireNonNull(value, "SpeciesId value must not be null");
  }

  public static SpeciesId newId() {
    return new SpeciesId(UUID.randomUUID());
  }
}
