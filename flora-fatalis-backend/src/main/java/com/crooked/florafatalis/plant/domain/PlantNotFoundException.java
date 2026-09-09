package com.crooked.florafatalis.plant.domain;

public class PlantNotFoundException extends RuntimeException {

  public PlantNotFoundException() {
    super("Plant not found");
  }
}
