package com.crooked.florafatalis.species.application.port.out;

import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.util.List;
import java.util.Optional;

public interface SpeciesRepository {

  List<Species> findAll();

  Optional<Species> findById(SpeciesId id);
}
