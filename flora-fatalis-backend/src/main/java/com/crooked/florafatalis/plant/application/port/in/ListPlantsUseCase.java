package com.crooked.florafatalis.plant.application.port.in;

import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.domain.UserId;
import java.util.List;

public interface ListPlantsUseCase {

  List<Plant> list(UserId userId);
}
