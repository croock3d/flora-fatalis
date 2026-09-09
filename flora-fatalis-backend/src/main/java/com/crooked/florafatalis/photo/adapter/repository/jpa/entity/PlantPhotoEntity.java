package com.crooked.florafatalis.photo.adapter.repository.jpa.entity;

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
@Table(name = "plant_photos")
@Getter
@Setter
@NoArgsConstructor
public class PlantPhotoEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "plant_id", nullable = false)
  private UUID plantId;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @Column(name = "storage_key", nullable = false)
  private String storageKey;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "taken_at", nullable = false)
  private Instant takenAt;

  @Column(name = "is_primary", nullable = false)
  private boolean primary;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;
}
