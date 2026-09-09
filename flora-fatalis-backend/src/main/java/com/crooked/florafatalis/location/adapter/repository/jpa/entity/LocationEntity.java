package com.crooked.florafatalis.location.adapter.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
public class LocationEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "kind", nullable = false)
  private String kind;
}
