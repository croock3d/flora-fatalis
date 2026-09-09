package com.crooked.florafatalis.photo.application.port.in;

import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface DeletePlantPhotoUseCase {

  void delete(UserId userId, PlantPhotoId photoId);
}
