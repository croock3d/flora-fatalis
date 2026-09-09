package com.crooked.florafatalis.plant.adapter.rest;

import com.crooked.florafatalis.household.application.port.out.UserLookupPort;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.application.port.in.ArchivePlantUseCase;
import com.crooked.florafatalis.plant.application.port.in.ArchivePlantUseCase.ArchivePlantCommand;
import com.crooked.florafatalis.plant.application.port.in.CreatePlantUseCase;
import com.crooked.florafatalis.plant.application.port.in.CreatePlantUseCase.CreatePlantCommand;
import com.crooked.florafatalis.plant.application.port.in.GetPlantUseCase;
import com.crooked.florafatalis.plant.application.port.in.GetPlantUseCase.GetPlantCommand;
import com.crooked.florafatalis.plant.application.port.in.ListPlantHistoryUseCase;
import com.crooked.florafatalis.plant.application.port.in.ListPlantHistoryUseCase.PlantHistoryItem;
import com.crooked.florafatalis.plant.application.port.in.ListPlantsUseCase;
import com.crooked.florafatalis.plant.application.port.in.UpdatePlantUseCase;
import com.crooked.florafatalis.plant.application.port.in.UpdatePlantUseCase.UpdatePlantCommand;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
class PlantController {

  private final ListPlantsUseCase listPlantsUseCase;
  private final GetPlantUseCase getPlantUseCase;
  private final CreatePlantUseCase createPlantUseCase;
  private final UpdatePlantUseCase updatePlantUseCase;
  private final ArchivePlantUseCase archivePlantUseCase;
  private final ListPlantHistoryUseCase listPlantHistoryUseCase;
  private final CurrentUserProvider currentUserProvider;
  private final UserLookupPort userLookupPort;

  @GetMapping
  List<PlantResponse> list() {
    return listPlantsUseCase.list(currentUserProvider.currentUserId()).stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  PlantResponse get(@PathVariable UUID id) {
    return toResponse(
        getPlantUseCase.get(
            new GetPlantCommand(currentUserProvider.currentUserId(), new PlantId(id))));
  }

  @GetMapping("/{id}/history")
  List<PlantHistoryResponse> history(@PathVariable UUID id) {
    return listPlantHistoryUseCase
        .list(currentUserProvider.currentUserId(), new PlantId(id))
        .stream()
        .map(this::toHistoryResponse)
        .toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  PlantResponse create(@RequestBody PlantRequest request) {
    return toResponse(
        createPlantUseCase.create(
            new CreatePlantCommand(
                currentUserProvider.currentUserId(),
                new SpeciesId(request.speciesId()),
                new LocationId(request.locationId()),
                request.name(),
                request.wateringIntervalDaysOverride(),
                request.fertilizingIntervalDaysOverride(),
                request.acquiredAt())));
  }

  @PutMapping("/{id}")
  PlantResponse update(@PathVariable UUID id, @RequestBody PlantRequest request) {
    return toResponse(
        updatePlantUseCase.update(
            new UpdatePlantCommand(
                currentUserProvider.currentUserId(),
                new PlantId(id),
                new SpeciesId(request.speciesId()),
                new LocationId(request.locationId()),
                request.name(),
                request.wateringIntervalDaysOverride(),
                request.fertilizingIntervalDaysOverride(),
                request.acquiredAt())));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void archive(@PathVariable UUID id) {
    archivePlantUseCase.archive(
        new ArchivePlantCommand(currentUserProvider.currentUserId(), new PlantId(id)));
  }

  private PlantHistoryResponse toHistoryResponse(PlantHistoryItem item) {
    String photoUrl =
        item.type() == ListPlantHistoryUseCase.HistoryType.PHOTO
            ? "/api/photos/" + item.sourceId()
            : null;
    String performedByName =
        item.performedBy() == null ? null : userLookupPort.findDisplayNameById(item.performedBy());
    return new PlantHistoryResponse(
        item.type().name(),
        item.occurredAt(),
        item.sourceId(),
        item.quantityMl(),
        item.performedBy() == null ? null : item.performedBy().value(),
        performedByName,
        photoUrl);
  }

  private PlantResponse toResponse(Plant plant) {
    return new PlantResponse(
        plant.id().value(),
        plant.speciesId().value(),
        plant.locationId().value(),
        plant.name(),
        plant.wateringIntervalDaysOverride(),
        plant.fertilizingIntervalDaysOverride(),
        plant.acquiredAt(),
        plant.archivedAt(),
        plant.createdAt());
  }

  record PlantRequest(
      UUID speciesId,
      UUID locationId,
      String name,
      Integer wateringIntervalDaysOverride,
      Integer fertilizingIntervalDaysOverride,
      Instant acquiredAt) {}

  record PlantResponse(
      UUID id,
      UUID speciesId,
      UUID locationId,
      String name,
      Integer wateringIntervalDaysOverride,
      Integer fertilizingIntervalDaysOverride,
      Instant acquiredAt,
      Instant archivedAt,
      Instant createdAt) {}

  record PlantHistoryResponse(
      String type,
      Instant occurredAt,
      UUID sourceId,
      Integer quantityMl,
      UUID performedBy,
      String performedByName,
      String photoUrl) {}
}
