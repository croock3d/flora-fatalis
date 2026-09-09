package com.crooked.florafatalis.photo.domain;

import java.util.Objects;
import java.util.UUID;

public record PlantPhotoId(UUID value) {

  public PlantPhotoId {
    Objects.requireNonNull(value, "PlantPhotoId value must not be null");
  }

  public static PlantPhotoId newId() {
    return new PlantPhotoId(UUID.randomUUID());
  }
}
