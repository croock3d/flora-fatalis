package com.crooked.florafatalis.household.domain;

public class HouseholdNotFoundException extends RuntimeException {

  public HouseholdNotFoundException() {
    super("Household not found");
  }
}
