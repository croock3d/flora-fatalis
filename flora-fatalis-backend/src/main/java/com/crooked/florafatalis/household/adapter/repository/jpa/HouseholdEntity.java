package com.crooked.florafatalis.household.adapter.repository.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "households")
@Getter
@Setter
@NoArgsConstructor
public class HouseholdEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Column(name = "partner_id")
  private UUID partnerId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
