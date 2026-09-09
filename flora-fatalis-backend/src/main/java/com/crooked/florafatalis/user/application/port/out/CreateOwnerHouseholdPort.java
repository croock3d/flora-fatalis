package com.crooked.florafatalis.user.application.port.out;

import com.crooked.florafatalis.shared.domain.UserId;

public interface CreateOwnerHouseholdPort {

  void createFor(UserId userId);
}
