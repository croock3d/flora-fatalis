package com.crooked.florafatalis.photo.domain;

public class PlantPhotoNotFoundException extends RuntimeException {

  public PlantPhotoNotFoundException() {
    super("Photo not found");
  }
}
