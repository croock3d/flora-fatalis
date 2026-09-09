package com.crooked.florafatalis.shared.application.port.out;

import com.crooked.florafatalis.shared.domain.UserId;

public interface CurrentUserProvider {

  UserId currentUserId();

  String displayName();
}
