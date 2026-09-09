package com.crooked.florafatalis.location.adapter.rest;

import com.crooked.florafatalis.location.application.port.in.CreateLocationUseCase;
import com.crooked.florafatalis.location.application.port.in.CreateLocationUseCase.CreateLocationCommand;
import com.crooked.florafatalis.location.application.port.in.DeleteLocationUseCase;
import com.crooked.florafatalis.location.application.port.in.DeleteLocationUseCase.DeleteLocationCommand;
import com.crooked.florafatalis.location.application.port.in.ListLocationsUseCase;
import com.crooked.florafatalis.location.application.port.in.UpdateLocationUseCase;
import com.crooked.florafatalis.location.application.port.in.UpdateLocationUseCase.UpdateLocationCommand;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.location.domain.LocationKind;
import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
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
@RequestMapping("/api/locations")
@RequiredArgsConstructor
class LocationController {

  private final ListLocationsUseCase listLocationsUseCase;
  private final CreateLocationUseCase createLocationUseCase;
  private final UpdateLocationUseCase updateLocationUseCase;
  private final DeleteLocationUseCase deleteLocationUseCase;
  private final CurrentUserProvider currentUserProvider;

  @GetMapping
  List<LocationResponse> list() {
    return listLocationsUseCase.list(currentUserProvider.currentUserId()).stream()
        .map(this::toResponse)
        .toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  LocationResponse create(@RequestBody LocationRequest request) {
    return toResponse(
        createLocationUseCase.create(
            new CreateLocationCommand(
                currentUserProvider.currentUserId(), request.name(), request.kind())));
  }

  @PutMapping("/{id}")
  LocationResponse update(@PathVariable UUID id, @RequestBody LocationRequest request) {
    return toResponse(
        updateLocationUseCase.update(
            new UpdateLocationCommand(
                currentUserProvider.currentUserId(),
                new LocationId(id),
                request.name(),
                request.kind())));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable UUID id) {
    deleteLocationUseCase.delete(
        new DeleteLocationCommand(currentUserProvider.currentUserId(), new LocationId(id)));
  }

  private LocationResponse toResponse(Location location) {
    return new LocationResponse(location.id().value(), location.name(), location.kind());
  }

  record LocationRequest(String name, String kind) {}

  record LocationResponse(UUID id, String name, LocationKind kind) {}
}
