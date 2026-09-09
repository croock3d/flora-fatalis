package com.crooked.florafatalis.species.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListSpeciesUseCaseHandlerTest {

  @Mock private SpeciesRepository speciesRepository;

  private ListSpeciesUseCaseHandler handler;

  @BeforeEach
  void setUp() {
    handler = new ListSpeciesUseCaseHandler(speciesRepository);
  }

  @Test
  void returnsSpeciesFromRepository() {
    Species other =
        new Species(
            SpeciesId.newId(),
            "Inny",
            null,
            7,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    given(speciesRepository.findAll()).willReturn(List.of(other));

    List<Species> result = handler.list();

    assertThat(result).containsExactly(other);
  }
}
