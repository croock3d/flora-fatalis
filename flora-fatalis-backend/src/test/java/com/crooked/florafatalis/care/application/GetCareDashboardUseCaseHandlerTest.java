package com.crooked.florafatalis.care.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

import com.crooked.florafatalis.care.application.port.in.GetCareDashboardUseCase.CareDashboard;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.care.domain.FertilizingCarePolicy;
import com.crooked.florafatalis.care.domain.IntervalCarePolicy;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
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
  @Mock private PlantPhotoRepository plantPhotoRepository;
  @Mock private LocationRepository locationRepository;

  private GetCareDashboardUseCaseHandler handler;

  private final Instant now = Instant.parse("2026-01-10T08:00:00Z");
  private final UserId userId = UserId.newId();
  private final HouseholdId householdId = HouseholdId.newId();
  private final SpeciesId speciesId = SpeciesId.newId();
  private final Species species =
      new Species(
          speciesId,
          "Monstera",
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

  @BeforeEach
  void setUp() {
    handler =
        new GetCareDashboardUseCaseHandler(
            plantRepository,
            speciesRepository,
            careEventRepository,
            new IntervalCarePolicy(),
            new FertilizingCarePolicy(),
            activeHouseholdPort,
            plantPhotoRepository,
            locationRepository,
            Clock.fixed(now, ZoneOffset.UTC));
    ReflectionTestUtils.setField(handler, "timezone", "UTC");
    lenient()
        .when(plantPhotoRepository.findPrimaryByHouseholdId(householdId))
        .thenReturn(List.of());
    lenient().when(locationRepository.findByHousehold(householdId)).thenReturn(List.of());
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
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.FERTILIZING))
        .willReturn(List.of());
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
    assertThat(dashboard.overdue().getFirst().careType()).isEqualTo("WATERING");
    assertThat(dashboard.overdue().getFirst().overdueDays()).isEqualTo(2);
    assertThat(dashboard.dueToday()).extracting(item -> item.plantName()).containsExactly("Fikus");
    assertThat(dashboard.upcoming()).extracting(item -> item.plantName()).containsExactly("Aloes");
    assertThat(dashboard.upcoming().getFirst().dueOn()).isEqualTo(LocalDate.parse("2026-01-12"));
  }

  @Test
  void usesPlantOverrideInsteadOfSpeciesInterval() {
    Plant plant = plant("Monstera", 3);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId)).willReturn(List.of(plant));
    given(speciesRepository.findAll()).willReturn(List.of(species));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.FERTILIZING))
        .willReturn(List.of());
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

  @Test
  void includesFertilizingTasksSeparatelyFromWatering() {
    Species fertilizingSpecies =
        new Species(
            speciesId,
            "Monstera",
            null,
            7,
            null,
            null,
            null,
            null,
            null,
            null,
            30,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null);
    Plant plant = plant("Fikus", null);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId)).willReturn(List.of(plant));
    given(speciesRepository.findAll()).willReturn(List.of(fertilizingSpecies));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.WATERING))
        .willReturn(
            List.of(
                CareEvent.watering(
                    plant.id(), householdId, userId, Instant.parse("2026-01-10T08:00:00Z"), null)));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.FERTILIZING))
        .willReturn(
            List.of(
                CareEvent.fertilizing(
                    plant.id(), householdId, userId, Instant.parse("2025-12-11T08:00:00Z"))));

    CareDashboard dashboard = handler.get(userId);

    assertThat(dashboard.dueToday())
        .extracting(item -> item.careType())
        .containsExactly("FERTILIZING");
    assertThat(dashboard.upcoming())
        .extracting(item -> item.careType())
        .containsExactly("WATERING");
  }

  @Test
  void returnsEmptyWhenHouseholdHasNoActivePlants() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId)).willReturn(List.of());
    given(speciesRepository.findAll()).willReturn(List.of(species));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.WATERING))
        .willReturn(List.of());
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.FERTILIZING))
        .willReturn(List.of());

    CareDashboard dashboard = handler.get(userId);

    assertThat(dashboard.overdue()).isEmpty();
    assertThat(dashboard.dueToday()).isEmpty();
    assertThat(dashboard.upcoming()).isEmpty();
  }

  @Test
  void noActiveHouseholdReturnsEmptyDashboard() {
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.empty());

    CareDashboard dashboard = handler.get(userId);

    assertThat(dashboard.overdue()).isEmpty();
    assertThat(dashboard.dueToday()).isEmpty();
    assertThat(dashboard.upcoming()).isEmpty();
    then(plantRepository).shouldHaveNoInteractions();
  }

  @Test
  void usesOnlyPlantsFromActiveHousehold() {
    HouseholdId otherHousehold = HouseholdId.newId();
    Plant own = plant("Monstera", null);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId)).willReturn(List.of(own));
    given(speciesRepository.findAll()).willReturn(List.of(species));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.WATERING))
        .willReturn(
            List.of(
                CareEvent.watering(
                    own.id(), householdId, userId, Instant.parse("2026-01-03T08:00:00Z"), null)));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.FERTILIZING))
        .willReturn(List.of());

    CareDashboard dashboard = handler.get(userId);

    assertThat(dashboard.dueToday())
        .extracting(item -> item.plantName())
        .containsExactly("Monstera");
    then(plantRepository).should().findActiveByHousehold(householdId);
    then(plantRepository).should(never()).findActiveByHousehold(otherHousehold);
    then(careEventRepository)
        .should()
        .findLatestByHouseholdAndCareType(householdId, CareType.WATERING);
    then(careEventRepository)
        .should(never())
        .findLatestByHouseholdAndCareType(otherHousehold, CareType.WATERING);
  }

  @Test
  void bucketsTasksUsingEuropeWarsawDay() {
    Instant lateUtc = Instant.parse("2026-01-09T23:30:00Z");
    GetCareDashboardUseCaseHandler warsawHandler =
        new GetCareDashboardUseCaseHandler(
            plantRepository,
            speciesRepository,
            careEventRepository,
            new IntervalCarePolicy(),
            new FertilizingCarePolicy(),
            activeHouseholdPort,
            plantPhotoRepository,
            locationRepository,
            Clock.fixed(lateUtc, ZoneOffset.UTC));
    ReflectionTestUtils.setField(warsawHandler, "timezone", "Europe/Warsaw");
    Plant plant = plant("Fikus", null);
    given(activeHouseholdPort.findActiveHouseholdId(userId)).willReturn(Optional.of(householdId));
    given(plantRepository.findActiveByHousehold(householdId)).willReturn(List.of(plant));
    given(speciesRepository.findAll()).willReturn(List.of(species));
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.FERTILIZING))
        .willReturn(List.of());
    given(careEventRepository.findLatestByHouseholdAndCareType(householdId, CareType.WATERING))
        .willReturn(
            List.of(
                CareEvent.watering(
                    plant.id(), householdId, userId, Instant.parse("2026-01-03T11:00:00Z"), null)));

    CareDashboard dashboard = warsawHandler.get(userId);

    assertThat(dashboard.dueToday()).extracting(item -> item.plantName()).containsExactly("Fikus");
    assertThat(dashboard.overdue()).isEmpty();
    assertThat(dashboard.upcoming()).isEmpty();
  }

  private Plant plant(String name, Integer override) {
    return Plant.create(
        householdId, speciesId, LocationId.newId(), name, override, null, null, userId, now);
  }
}
