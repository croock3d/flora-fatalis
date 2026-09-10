package com.crooked.florafatalis.photo.application;

import com.crooked.florafatalis.photo.application.port.in.UploadPlantPhotoUseCase;
import com.crooked.florafatalis.photo.application.port.out.PhotoStorage;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.plant.domain.Plant;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadPlantPhotoUseCaseHandler implements UploadPlantPhotoUseCase {

  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
  private static final int MAX_BYTES = 3 * 1024 * 1024;

  private final PlantPhotoAccess plantPhotoAccess;
  private final PlantPhotoRepository plantPhotoRepository;
  private final PhotoStorage photoStorage;
  private final Clock clock;

  @Override
  @Transactional
  public PlantPhoto upload(UploadPlantPhotoCommand command) {
    if (command.content() == null || command.content().length == 0) {
      throw new IllegalArgumentException("photo content must not be empty");
    }
    if (command.content().length > MAX_BYTES) {
      throw new IllegalArgumentException("photo is too large");
    }
    String contentType = normalizeContentType(command.contentType());
    if (!ALLOWED_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("unsupported photo type");
    }
    Plant plant = plantPhotoAccess.requirePlant(command.userId(), command.plantId());
    boolean primary = plantPhotoRepository.findPrimaryByPlantId(plant.id()).isEmpty();
    String extension = extensionFor(contentType);
    String storageKey =
        plant.householdId().value()
            + "/"
            + plant.id().value()
            + "/"
            + UUID.randomUUID()
            + extension;
    photoStorage.store(storageKey, command.content());
    PlantPhoto photo =
        PlantPhoto.create(
            plant.id(),
            plant.householdId(),
            storageKey,
            contentType,
            Instant.now(clock),
            primary,
            0);
    plantPhotoRepository.save(photo);
    return photo;
  }

  private String normalizeContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      return "image/jpeg";
    }
    return contentType.toLowerCase().split(";")[0].strip();
  }

  private String extensionFor(String contentType) {
    return switch (contentType) {
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      default -> ".jpg";
    };
  }
}
