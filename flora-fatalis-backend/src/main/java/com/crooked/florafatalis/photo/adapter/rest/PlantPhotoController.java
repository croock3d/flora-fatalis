package com.crooked.florafatalis.photo.adapter.rest;

import com.crooked.florafatalis.photo.application.port.in.DeletePlantPhotoUseCase;
import com.crooked.florafatalis.photo.application.port.in.GetPlantPhotoContentUseCase;
import com.crooked.florafatalis.photo.application.port.in.GetPlantPhotoContentUseCase.PhotoContent;
import com.crooked.florafatalis.photo.application.port.in.ListPlantPhotosUseCase;
import com.crooked.florafatalis.photo.application.port.in.SetPrimaryPlantPhotoUseCase;
import com.crooked.florafatalis.photo.application.port.in.UploadPlantPhotoUseCase;
import com.crooked.florafatalis.photo.application.port.in.UploadPlantPhotoUseCase.UploadPlantPhotoCommand;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.application.port.out.CurrentUserProvider;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
class PlantPhotoController {

  private final UploadPlantPhotoUseCase uploadPlantPhotoUseCase;
  private final ListPlantPhotosUseCase listPlantPhotosUseCase;
  private final GetPlantPhotoContentUseCase getPlantPhotoContentUseCase;
  private final SetPrimaryPlantPhotoUseCase setPrimaryPlantPhotoUseCase;
  private final DeletePlantPhotoUseCase deletePlantPhotoUseCase;
  private final CurrentUserProvider currentUserProvider;

  @GetMapping("/plants/{plantId}/photos")
  List<PlantPhotoResponse> list(@PathVariable UUID plantId) {
    return listPlantPhotosUseCase
        .list(currentUserProvider.currentUserId(), new PlantId(plantId))
        .stream()
        .map(this::toResponse)
        .toList();
  }

  @PostMapping(path = "/plants/{plantId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  PlantPhotoResponse upload(@PathVariable UUID plantId, @RequestParam("file") MultipartFile file)
      throws IOException {
    return toResponse(
        uploadPlantPhotoUseCase.upload(
            new UploadPlantPhotoCommand(
                currentUserProvider.currentUserId(),
                new PlantId(plantId),
                file.getContentType(),
                file.getBytes())));
  }

  @GetMapping("/photos/{id}")
  ResponseEntity<byte[]> content(@PathVariable UUID id) {
    PhotoContent content =
        getPlantPhotoContentUseCase.get(currentUserProvider.currentUserId(), new PlantPhotoId(id));
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(content.photo().contentType()))
        .body(content.bytes());
  }

  @PostMapping("/photos/{id}/primary")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void setPrimary(@PathVariable UUID id) {
    setPrimaryPlantPhotoUseCase.setPrimary(
        currentUserProvider.currentUserId(), new PlantPhotoId(id));
  }

  @DeleteMapping("/photos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable UUID id) {
    deletePlantPhotoUseCase.delete(currentUserProvider.currentUserId(), new PlantPhotoId(id));
  }

  private PlantPhotoResponse toResponse(PlantPhoto photo) {
    return new PlantPhotoResponse(
        photo.id().value(),
        photo.plantId().value(),
        photo.primary(),
        photo.takenAt(),
        "/api/photos/" + photo.id().value());
  }

  record PlantPhotoResponse(UUID id, UUID plantId, boolean primary, Instant takenAt, String url) {}
}
