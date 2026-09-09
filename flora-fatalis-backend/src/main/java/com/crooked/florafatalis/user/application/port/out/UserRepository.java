package com.crooked.florafatalis.user.application.port.out;

import com.crooked.florafatalis.user.domain.User;
import java.util.Optional;

public interface UserRepository {

  void save(User user);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
