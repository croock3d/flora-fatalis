package com.crooked.florafatalis.species.application.port.in;

import com.crooked.florafatalis.species.domain.Species;
import java.util.List;

public interface ListSpeciesUseCase {

  List<Species> list();
}
