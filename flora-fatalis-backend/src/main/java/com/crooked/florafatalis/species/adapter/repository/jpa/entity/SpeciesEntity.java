package com.crooked.florafatalis.species.adapter.repository.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "species")
@Getter
@Setter
@NoArgsConstructor
public class SpeciesEntity {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "latin_name")
  private String latinName;

  @Column(name = "default_watering_interval_days", nullable = false)
  private int defaultWateringIntervalDays;

  @Column(name = "watering_interval_min_days")
  private Integer wateringIntervalMinDays;

  @Column(name = "watering_interval_max_days")
  private Integer wateringIntervalMaxDays;

  @Column(name = "watering_interval_label")
  private String wateringIntervalLabel;

  @Column(name = "light_preference")
  private String lightPreference;

  @Column(name = "humidity_preference")
  private String humidityPreference;

  @Column(name = "category")
  private String category;

  @Column(name = "default_fertilizing_interval_days")
  private Integer defaultFertilizingIntervalDays;

  @Column(name = "fertilizing_interval_min_days")
  private Integer fertilizingIntervalMinDays;

  @Column(name = "fertilizing_interval_max_days")
  private Integer fertilizingIntervalMaxDays;

  @Column(name = "fertilizing_interval_label")
  private String fertilizingIntervalLabel;

  @Column(name = "fertilizing_season")
  private String fertilizingSeason;

  @Column(name = "fertilizer_type")
  private String fertilizerType;

  @Column(name = "fertilizer_form")
  private String fertilizerForm;

  @Column(name = "fertilizing_notes")
  private String fertilizingNotes;

  @Column(name = "fertilizing_rest_period")
  private String fertilizingRestPeriod;
}
