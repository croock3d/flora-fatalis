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
@Table(name = "household_invitations")
@Getter
@Setter
@NoArgsConstructor
public class HouseholdInvitationEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @Column(name = "inviter_id", nullable = false)
  private UUID inviterId;

  @Column(name = "invitee_id", nullable = false)
  private UUID inviteeId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
