package com.crooked.florafatalis.care.adapter.repository.jpa.entity;

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
@Table(name = "care_events")
@Getter
@Setter
@NoArgsConstructor
public class CareEventEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "plant_id", nullable = false)
  private UUID plantId;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @Column(name = "care_type", nullable = false)
  private String careType;

  @Column(name = "performed_at", nullable = false)
  private Instant performedAt;

  @Column(name = "performed_by", nullable = false)
  private UUID performedBy;

  @Column(name = "quantity_ml")
  private Integer quantityMl;

  @Column(name = "source", nullable = false)
  private String source;

  @Column(name = "notes")
  private String notes;

  @Column(name = "pruning_kind")
  private String pruningKind;
}
