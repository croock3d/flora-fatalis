package com.crooked.florafatalis.household.domain;

public class HouseholdAlreadyExistsException extends RuntimeException {

  public HouseholdAlreadyExistsException() {
    super("User already has an active or pending household");
  }
}
