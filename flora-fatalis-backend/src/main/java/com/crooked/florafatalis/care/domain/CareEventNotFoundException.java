package com.crooked.florafatalis.care.domain;

public class CareEventNotFoundException extends RuntimeException {

  public CareEventNotFoundException() {
    super("Care event not found");
  }
}
