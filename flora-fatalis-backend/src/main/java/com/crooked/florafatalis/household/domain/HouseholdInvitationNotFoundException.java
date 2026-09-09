package com.crooked.florafatalis.household.domain;

public class HouseholdInvitationNotFoundException extends RuntimeException {

  public HouseholdInvitationNotFoundException() {
    super("Household invitation not found");
  }
}
