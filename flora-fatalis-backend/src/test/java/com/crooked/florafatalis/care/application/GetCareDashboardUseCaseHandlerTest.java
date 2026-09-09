package com.crooked.florafatalis.care.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.crooked.florafatalis.care.application.port.in.GetCareDashboardUseCase.CareDashboard;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.care.domain.IntervalCarePolicy;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GetCareDashboardUseCaseHandlerTest {

  @Mock private PlantRepository plantRepository;
  @Mock private SpeciesRepository speciesRepository;
  @Mock private CareEventRepository careEventRepository;
  @Mock private ActiveHouseholdPort activeHouseholdPort;

  private GetCareDashboardUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-10T08:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final Species species =
      new Species(speciesId, "Monstera", null, 7, null, null, null, null, null, null);

  @BeforeEach
  void setUp() {
    handler =
        new GetCareDashboardUseCaseHandler(
            plantRepository,
            speciesRepository,
            careEventRepository,
            new IntervalCarePolicy(),
            activeHouseholdPort,
            Clock.fixed(now, ZoneOffset.UTC));
    ReflectionTestUtils.setField(handler, "timezone", "UTC");
  }

  @Test
  void splitsOverdueTodayAndUpcomingWithinSevenDays() {
    LocalDate today = LocalDate.parse("2026-01-10");
    Plant overdue = plant("Monstera", null);
    Plant dueToday = plant("Fikus", null);
    Plant upcoming = plant("Aloes", 3);
    Plant later = plant("Kaktus", 14);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId))
        .willReturn(List.of(overdue, dueToday, upcoming, later));
    given(speciesRepository.findAll()).willReturn(List.of(species));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.WATERING))
        .willReturn(
            List.of(
                CareEvent.watering(
                    overdue.id(),
                    householdId,
                    userId,
                    today.minusDays(9).atStartOfDay().toInstant(ZoneOffset.UTC),
                    null),
                CareEvent.watering(
                    dueToday.id(),
                    householdId,
                    userId,
                    today.minusDays(7).atStartOfDay().toInstant(ZoneOffset.UTC),
                    null),
                CareEvent.watering(
                    upcoming.id(),
                    householdId,
                    userId,
                    today.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                    null),
                CareEvent.watering(
                    later.id(),
                    householdId,
                    userId,
                    today.minusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                    null)));

    CareDashboard dashboard = handler.get(userId);

    assertThat(dashboard.overdue()).hasSize(1);
    assertThat(dashboard.overdue().getFirst().plantName()).isEqualTo("Monstera");
    assertThat(dashboard.overdue().getFirst().overdueDays()).isEqualTo(2);
    assertThat(dashboard.dueToday()).extracting(item -> item.plantName()).containsExactly("Fikus");
    assertThat(dashboard.upcoming()).extracting(item -> item.plantName()).containsExactly("Aloes");
  }

  @Test
  void usesPlantOverrideInsteadOfSpeciesInterval() {
    Plant plant = plant("Monstera", 3);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId)).willReturn(List.of(plant));
    given(speciesRepository.findAll()).willReturn(List.of(species));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.WATERING))
        .willReturn(
            List.of(
                CareEvent.watering(
                    plant.id(), householdId, userId, Instant.parse("2026-01-05T08:00:00Z"), null)));

    CareDashboard dashboard = handler.get(userId);

    assertThat(dashboard.overdue()).hasSize(1);
    assertThat(dashboard.overdue().getFirst().overdueDays()).isEqualTo(2);
    assertThat(dashboard.dueToday()).isEmpty();
  }

  private Plant plant(String name, Integer override) {
    return Plant.create(
        householdId, speciesId, LocationId.newId(), name, override, null, userId, now);
  }
}
