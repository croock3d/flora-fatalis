package com.crooked.florafatalis.location.domain;

public class LocationInUseException extends RuntimeException {

  public LocationInUseException() {
    super("Location is in use");
  }
}
