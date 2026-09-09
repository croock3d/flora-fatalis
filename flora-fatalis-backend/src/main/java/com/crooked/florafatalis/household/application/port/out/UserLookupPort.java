package com.crooked.florafatalis.household.application.port.out;

import com.crooked.florafatalis.shared.domain.UserId;
import java.util.Optional;

public interface UserLookupPort {

  Optional<UserId> findUserIdByEmail(String email);

  String findDisplayNameById(UserId userId);
}
