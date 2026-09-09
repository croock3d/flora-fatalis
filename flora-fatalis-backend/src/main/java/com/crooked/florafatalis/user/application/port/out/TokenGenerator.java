package com.crooked.florafatalis.user.application.port.out;

import com.crooked.florafatalis.user.domain.AuthToken;
import com.crooked.florafatalis.user.domain.User;

public interface TokenGenerator {

  AuthToken generate(User user);
}
