package com.crooked.florafatalis.photo.application.port.in;

import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;

public interface ListPlantPhotosUseCase {

  List<PlantPhoto> list(UserId userId, PlantId plantId);
}
