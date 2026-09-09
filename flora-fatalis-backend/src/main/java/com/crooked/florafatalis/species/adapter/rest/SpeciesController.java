package com.crooked.florafatalis.species.adapter.rest;

import com.crooked.florafatalis.species.application.port.in.ListSpeciesUseCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
class SpeciesController {

  private final ListSpeciesUseCase listSpeciesUseCase;

  @GetMapping
  List<SpeciesResponse> list() {
    return listSpeciesUseCase.list().stream()
        .map(
            species ->
                new SpeciesResponse(
                    species.id().value(),
                    species.name(),
                    species.latinName(),
                    species.defaultWateringIntervalDays(),
                    species.wateringIntervalMinDays(),
                    species.wateringIntervalMaxDays(),
                    species.wateringIntervalLabel(),
                    species.lightPreference(),
                    species.humidityPreference(),
                    species.category()))
        .toList();
  }

  record SpeciesResponse(
      UUID id,
      String name,
      String latinName,
      int defaultWateringIntervalDays,
      Integer wateringIntervalMinDays,
      Integer wateringIntervalMaxDays,
      String wateringIntervalLabel,
      String lightPreference,
      String humidityPreference,
      String category) {}
}
