package com.crooked.florafatalis.photo.application.port.in;

import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.photo.domain.PlantPhotoId;
import com.crooked.florafatalis.shared.domain.UserId;

public interface GetPlantPhotoContentUseCase {

  PhotoContent get(UserId userId, PlantPhotoId photoId);

  record PhotoContent(PlantPhoto photo, byte[] bytes) {}
}
