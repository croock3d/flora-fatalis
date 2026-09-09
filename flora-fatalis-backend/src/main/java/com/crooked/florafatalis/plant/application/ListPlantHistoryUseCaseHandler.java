package com.crooked.florafatalis.plant.application;

import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.household.domain.HouseholdAccessDeniedException;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.household.domain.NoActiveHouseholdException;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.plant.application.port.in.ListPlantHistoryUseCase;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.plant.domain.PlantNotFoundException;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPlantHistoryUseCaseHandler implements ListPlantHistoryUseCase {

  private final PlantRepository plantRepository;
  private final CareEventRepository careEventRepository;
  private final PlantPhotoRepository plantPhotoRepository;
  private final ActiveHouseholdPort activeHouseholdPort;

  @Override
  public List<PlantHistoryItem> list(UserId userId, PlantId plantId) {
    HouseholdId householdId =
        activeHouseholdPort
            .findActiveHouseholdId(userId)
            .orElseThrow(NoActiveHouseholdException::new);
    Plant plant = plantRepository.findById(plantId).orElseThrow(PlantNotFoundException::new);
    if (!plant.householdId().equals(householdId)) {
      throw new HouseholdAccessDeniedException();
    }
    List<PlantHistoryItem> items = new ArrayList<>();
    items.add(
        new PlantHistoryItem(
            HistoryType.CREATED,
            plant.createdAt(),
            plant.id().value(),
            null,
            plant.createdBy(),
            null,
            null));
    addCareEvents(items, plantId, CareType.WATERING, HistoryType.WATERING);
    addCareEvents(items, plantId, CareType.FERTILIZING, HistoryType.FERTILIZING);
    addCareEvents(items, plantId, CareType.PRUNING, HistoryType.PRUNING);
    plantPhotoRepository
        .findByPlantId(plantId)
        .forEach(
            photo ->
                items.add(
                    new PlantHistoryItem(
                        HistoryType.PHOTO,
                        photo.takenAt(),
                        photo.id().value(),
                        null,
                        null,
                        null,
                        null)));
    items.sort(
        Comparator.comparing(PlantHistoryItem::occurredAt)
            .reversed()
            .thenComparing(item -> item.type().ordinal()));
    return items;
  }

  private void addCareEvents(
      List<PlantHistoryItem> items, PlantId plantId, CareType careType, HistoryType historyType) {
    careEventRepository
        .findByPlantIdAndCareType(plantId, careType)
        .forEach(
            event ->
                items.add(
                    new PlantHistoryItem(
                        historyType,
                        event.performedAt(),
                        event.id().value(),
                        event.quantityMl(),
                        event.performedBy(),
                        event.notes(),
                        event.pruningKind())));
  }
}
