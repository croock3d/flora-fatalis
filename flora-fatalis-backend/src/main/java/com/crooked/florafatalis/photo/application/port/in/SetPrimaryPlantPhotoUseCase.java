package com.crooked.florafatalis.photo.application.port.in;

import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface SetPrimaryPlantPhotoUseCase {

  void setPrimary(UserId userId, PlantPhotoId photoId);
}
