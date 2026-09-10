package com.crooked.florafatalis.household.domain;

public class HouseholdAlreadyExistsException extends RuntimeException {

  public HouseholdAlreadyExistsException() {
    super("Household invitation is not allowed");
  }
}
