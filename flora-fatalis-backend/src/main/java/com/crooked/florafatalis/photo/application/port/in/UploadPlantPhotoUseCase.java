package com.crooked.florafatalis.photo.application.port.in;

import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface UploadPlantPhotoUseCase {

  PlantPhoto upload(UploadPlantPhotoCommand command);

  record UploadPlantPhotoCommand(
      UserId userId,
      PlantId plantId,
      String originalFilename,
      String contentType,
      byte[] content) {}
}
