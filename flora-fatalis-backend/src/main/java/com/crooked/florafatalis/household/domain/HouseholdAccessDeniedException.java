package com.crooked.florafatalis.household.domain;

public class HouseholdAccessDeniedException extends RuntimeException {

  public HouseholdAccessDeniedException() {
    super("Access denied to household operation");
  }
}
