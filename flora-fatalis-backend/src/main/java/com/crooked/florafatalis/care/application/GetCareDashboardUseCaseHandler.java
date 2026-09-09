package com.crooked.florafatalis.care.application;

import com.crooked.florafatalis.care.application.port.in.GetCareDashboardUseCase;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CarePolicy;
import com.crooked.florafatalis.care.domain.CareRecommendation;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCareDashboardUseCaseHandler implements GetCareDashboardUseCase {

  private final PlantRepository plantRepository;
  private final SpeciesRepository speciesRepository;
  private final CareEventRepository careEventRepository;
  private final CarePolicy carePolicy;
  private final ActiveHouseholdPort activeHouseholdPort;
  private final Clock clock;

  @Value("${app.timezone:Europe/Warsaw}")
  private String timezone;

  @Override
  public CareDashboard get(UserId userId) {
    ZoneId zone = ZoneId.of(timezone);
    LocalDate today = LocalDate.now(clock.withZone(zone));
    return activeHouseholdPort
        .findActiveHouseholdId(userId)
        .map(householdId -> build(householdId, today, zone))
        .orElse(new CareDashboard(List.of(), List.of(), List.of()));
  }

  private CareDashboard build(
      com.crooked.florafatalis.household.domain.HouseholdId householdId,
      LocalDate today,
      ZoneId zone) {
    List<Plant> plants = plantRepository.findActiveByHousehold(householdId);
    Map<com.crooked.florafatalis.species.domain.SpeciesId, Species> speciesById =
        speciesRepository.findAll().stream()
            .collect(Collectors.toMap(Species::id, Function.identity()));
    Map<com.crooked.florafatalis.plant.domain.PlantId, CareEvent> latestByPlant =
        careEventRepository
            .findLatestByHouseholdAndCareType(householdId, CareType.WATERING)
            .stream()
            .collect(
                Collectors.toMap(CareEvent::plantId, Function.identity(), (left, right) -> left));
    List<DashboardItem> overdue = new ArrayList<>();
    List<DashboardItem> dueToday = new ArrayList<>();
    List<DashboardItem> upcoming = new ArrayList<>();
    for (Plant plant : plants) {
      Species species = speciesById.get(plant.speciesId());
      if (species == null) {
        continue;
      }
      Optional<java.time.Instant> last =
          Optional.ofNullable(latestByPlant.get(plant.id())).map(CareEvent::performedAt);
      CareRecommendation recommendation = carePolicy.recommend(plant, species, last, today, zone);
      long days = java.time.temporal.ChronoUnit.DAYS.between(recommendation.dueOn(), today);
      DashboardItem item =
          DashboardItem.of(
              plant.id(), plant.name(), recommendation.dueOn(), (int) Math.max(days, 0));
      if (recommendation.dueOn().isBefore(today)) {
        overdue.add(item);
      } else if (recommendation.dueOn().isEqual(today)) {
        dueToday.add(item);
      } else if (!recommendation.dueOn().isAfter(today.plusDays(7))) {
        upcoming.add(item);
      }
    }
    overdue.sort(Comparator.comparing(DashboardItem::dueOn));
    dueToday.sort(Comparator.comparing(DashboardItem::plantName));
    upcoming.sort(Comparator.comparing(DashboardItem::dueOn));
    return new CareDashboard(overdue, dueToday, upcoming);
  }
}
