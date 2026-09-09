package com.crooked.florafatalis.species.domain;

public class SpeciesNotFoundException extends RuntimeException {

  public SpeciesNotFoundException() {
    super("Species not found");
  }
}
