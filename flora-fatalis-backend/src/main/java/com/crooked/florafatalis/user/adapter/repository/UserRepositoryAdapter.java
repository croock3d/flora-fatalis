package com.crooked.florafatalis.user.adapter.repository;

import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.user.adapter.repository.jpa.UserJpaRepository;
import com.crooked.florafatalis.user.adapter.repository.jpa.entity.UserEntity;
import com.crooked.florafatalis.user.application.port.out.UserRepository;
import com.crooked.florafatalis.user.domain.DisplayName;
import com.crooked.florafatalis.user.domain.Email;
import com.crooked.florafatalis.user.domain.HashedPassword;
import com.crooked.florafatalis.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

  private final UserJpaRepository jpaRepository;

  @Override
  public void save(User user) {
    UserEntity entity = jpaRepository.findById(user.id().value()).orElseGet(UserEntity::new);
    entity.setId(user.id().value());
    entity.setEmail(user.email().value());
    entity.setHashedPassword(user.hashedPassword().value());
    entity.setDisplayName(user.displayName().value());
    entity.setCreatedAt(user.createdAt());
    jpaRepository.save(entity);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpaRepository.findByEmail(email).map(this::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaRepository.existsByEmail(email);
  }

  private User toDomain(UserEntity entity) {
    return User.reconstitute(
        new UserId(entity.getId()),
        new Email(entity.getEmail()),
        new HashedPassword(entity.getHashedPassword()),
        new DisplayName(entity.getDisplayName()),
        entity.getCreatedAt());
  }
}
