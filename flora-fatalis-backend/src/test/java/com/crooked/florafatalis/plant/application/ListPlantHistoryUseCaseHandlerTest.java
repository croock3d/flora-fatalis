package com.crooked.florafatalis.plant.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.plant.application.port.in.ListPlantHistoryUseCase.HistoryType;
import com.crooked.florafatalis.plant.application.port.in.ListPlantHistoryUseCase.PlantHistoryItem;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListPlantHistoryUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private CareEventRepository careEventRepository;
  @Mock private PlantPhotoRepository plantPhotoRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private ListPlantHistoryUseCaseHandler handler;

  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();
  private final Instant createdAt = Instant.parse("2026-08-20T08:00:00Z");

  @BeforeEach
  void setUp() {
    handler =
        new ListPlantHistoryUseCaseHandler(
            plantRepository, careEventRepository, plantPhotoRepository, activeHouseholdPort);
  }

  @Test
  void returnsEventsForPlantSortedNewestFirst() {
    Plant plant = plant();
    CareEvent wateringWithMl =
        CareEvent.watering(
            plant.id(), householdId, userId, Instant.parse("2026-09-08T17:30:00Z"), 500);
    CareEvent wateringWithoutMl =
        CareEvent.watering(
            plant.id(), householdId, userId, Instant.parse("2026-09-03T18:10:00Z"), null);
    PlantPhoto photo =
        PlantPhoto.create(
            plant.id(),
            householdId,
            "key",
            "image/jpeg",
            Instant.parse("2026-09-09T10:00:00Z"),
            true,
            0);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));
    CareEvent fertilizing =
        CareEvent.fertilizing(
            plant.id(), householdId, userId, Instant.parse("2026-09-08T16:40:00Z"));
    given(careEventRepository.findByPlantIdAndCareType(plant.id(), CareType.WATERING))
        .willReturn(List.of(wateringWithMl, wateringWithoutMl));
    given(careEventRepository.findByPlantIdAndCareType(plant.id(), CareType.FERTILIZING))
        .willReturn(List.of(fertilizing));
    given(plantPhotoRepository.findByPlantId(plant.id())).willReturn(List.of(photo));

    List<PlantHistoryItem> items = handler.list(userId, plant.id());

    assertThat(items).hasSize(5);
    assertThat(items)
        .extracting(PlantHistoryItem::type)
        .containsExactly(
            HistoryType.PHOTO,
            HistoryType.WATERING,
            HistoryType.FERTILIZING,
            HistoryType.WATERING,
            HistoryType.CREATED);
    assertThat(items.get(0).sourceId()).isEqualTo(photo.id().value());
    assertThat(items.get(1).quantityMl()).isEqualTo(500);
    assertThat(items.get(2).type()).isEqualTo(HistoryType.FERTILIZING);
    assertThat(items.get(3).quantityMl()).isNull();
    assertThat(items.get(4).occurredAt()).isEqualTo(createdAt);
    assertThat(items).filteredOn(item -> item.type() == HistoryType.WATERING).hasSize(2);
    assertThat(items).filteredOn(item -> item.type() == HistoryType.FERTILIZING).hasSize(1);
    assertThat(items).filteredOn(item -> item.type() == HistoryType.PHOTO).hasSize(1);
    assertThat(items).filteredOn(item -> item.type() == HistoryType.CREATED).hasSize(1);
  }

  @Test
  void plantFromOtherHouseholdThrows() {
    Plant plant =
        Plant.create(
            HouseholdId.newId(),
            SpeciesId.newId(),
            LocationId.newId(),
            "Monstera",
            null,
            null,
            null,
            userId,
            createdAt);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findById(plant.id())).willReturn(Optional.of(plant));

    assertThatThrownBy(() -> handler.list(userId, plant.id()))
        .isInstanceOf(HouseholdAccessDeniedException.class);
  }

  private Plant plant() {
    return Plant.create(
        householdId,
        SpeciesId.newId(),
        LocationId.newId(),
        "Monstera",
        null,
        null,
        null,
        userId,
        createdAt);
  }
}
