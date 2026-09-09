package com.crooked.florafatalis.care.adapter.rest;

import com.crooked.florafatalis.care.application.port.in.DeleteCareEventUseCase;
import com.crooked.florafatalis.care.application.port.in.DeleteCareEventUseCase.DeleteCareEventCommand;
import com.crooked.florafatalis.care.application.port.in.FertilizePlantUseCase;
import com.crooked.florafatalis.care.application.port.in.FertilizePlantUseCase.FertilizePlantCommand;
import com.crooked.florafatalis.care.application.port.in.GetCareDashboardUseCase;
import com.crooked.florafatalis.care.application.port.in.GetCareDashboardUseCase.CareDashboard;
import com.crooked.florafatalis.care.application.port.in.GetPlantCareStatusUseCase;
import com.crooked.florafatalis.care.application.port.in.GetPlantCareStatusUseCase.PlantCareStatus;
import com.crooked.florafatalis.care.application.port.in.PrunePlantUseCase;
import com.crooked.florafatalis.care.application.port.in.PrunePlantUseCase.PrunePlantCommand;
import com.crooked.florafatalis.care.application.port.in.WaterPlantUseCase;
import com.crooked.florafatalis.care.application.port.in.WaterPlantUseCase.WaterPlantCommand;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareEventId;
import com.crooked.florafatalis.care.domain.PruningKind;
import com.crooked.florafatalis.household.application.port.out.UserLookupPort;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class CareController {

  private final WaterPlantUseCase waterPlantUseCase;
  private final FertilizePlantUseCase fertilizePlantUseCase;
  private final PrunePlantUseCase prunePlantUseCase;
  private final DeleteCareEventUseCase deleteCareEventUseCase;
  private final GetCareDashboardUseCase getCareDashboardUseCase;
  private final GetPlantCareStatusUseCase getPlantCareStatusUseCase;
  private final CurrentUserProvider currentUserProvider;
  private final UserLookupPort userLookupPort;

  @PostMapping("/plants/{plantId}/water")
  @ResponseStatus(HttpStatus.CREATED)
  CareEventResponse water(
      @PathVariable UUID plantId, @RequestBody(required = false) WaterPlantRequest request) {
    Integer quantityMl = request == null ? null : request.quantityMl();
    return toResponse(
        waterPlantUseCase.water(
            new WaterPlantCommand(
                currentUserProvider.currentUserId(), new PlantId(plantId), quantityMl)));
  }

  @PostMapping("/plants/{plantId}/fertilize")
  @ResponseStatus(HttpStatus.CREATED)
  CareEventResponse fertilize(@PathVariable UUID plantId) {
    return toResponse(
        fertilizePlantUseCase.fertilize(
            new FertilizePlantCommand(currentUserProvider.currentUserId(), new PlantId(plantId))));
  }

  @PostMapping("/plants/{plantId}/prune")
  @ResponseStatus(HttpStatus.CREATED)
  CareEventResponse prune(
      @PathVariable UUID plantId, @RequestBody(required = false) PrunePlantRequest request) {
    PrunePlantRequest body = request == null ? new PrunePlantRequest(null, null, null) : request;
    return toResponse(
        prunePlantUseCase.prune(
            new PrunePlantCommand(
                currentUserProvider.currentUserId(),
                new PlantId(plantId),
                body.performedOn(),
                body.pruningKind(),
                body.notes())));
  }

  @DeleteMapping("/care-events/{eventId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable UUID eventId) {
    deleteCareEventUseCase.delete(
        new DeleteCareEventCommand(currentUserProvider.currentUserId(), new CareEventId(eventId)));
  }

  @GetMapping("/dashboard")
  CareDashboard dashboard() {
    return getCareDashboardUseCase.get(currentUserProvider.currentUserId());
  }

  @GetMapping("/plants/{plantId}/care-status")
  PlantCareStatus careStatus(@PathVariable UUID plantId) {
    return getPlantCareStatusUseCase.get(currentUserProvider.currentUserId(), new PlantId(plantId));
  }

  private CareEventResponse toResponse(CareEvent event) {
    return new CareEventResponse(
        event.id().value(),
        event.plantId().value(),
        event.careType().name(),
        event.performedAt(),
        event.performedBy().value(),
        userLookupPort.findDisplayNameById(event.performedBy()),
        event.quantityMl(),
        event.notes(),
        event.pruningKind() == null ? null : event.pruningKind().name());
  }

  record WaterPlantRequest(Integer quantityMl) {}

  record PrunePlantRequest(LocalDate performedOn, PruningKind pruningKind, String notes) {}

  record CareEventResponse(
      UUID id,
      UUID plantId,
      String careType,
      Instant performedAt,
      UUID performedBy,
      String performedByName,
      Integer quantityMl,
      String notes,
      String pruningKind) {}
}
