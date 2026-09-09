package com.crooked.florafatalis.plant.adapter.repository.jpa.entity;

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
@Table(name = "plants")
@Getter
@Setter
@NoArgsConstructor
public class PlantEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @Column(name = "species_id", nullable = false)
  private UUID speciesId;

  @Column(name = "location_id", nullable = false)
  private UUID locationId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "watering_interval_days_override")
  private Integer wateringIntervalDaysOverride;

  @Column(name = "fertilizing_interval_days_override")
  private Integer fertilizingIntervalDaysOverride;

  @Column(name = "acquired_at")
  private Instant acquiredAt;

  @Column(name = "archived_at")
  private Instant archivedAt;

  @Column(name = "created_by", nullable = false)
  private UUID createdBy;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
