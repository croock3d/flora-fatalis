package com.crooked.florafatalis.species.application;

import com.crooked.florafatalis.species.application.port.in.ListSpeciesUseCase;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListSpeciesUseCaseHandler implements ListSpeciesUseCase {

  private final SpeciesRepository speciesRepository;

  @Override
  public List<Species> list() {
    return speciesRepository.findAll();
  }
}
