package com.crooked.florafatalis.household.domain;

public class NoActiveHouseholdException extends RuntimeException {

  public NoActiveHouseholdException() {
    super("No active household");
  }
}
