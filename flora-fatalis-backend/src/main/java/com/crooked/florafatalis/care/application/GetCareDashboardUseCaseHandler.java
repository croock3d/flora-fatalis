package com.crooked.florafatalis.care.application;

import com.crooked.florafatalis.care.application.port.in.GetCareDashboardUseCase;
import com.crooked.florafatalis.care.application.port.out.CareEventRepository;
import com.crooked.florafatalis.care.domain.CareEvent;
import com.crooked.florafatalis.care.domain.CarePolicy;
import com.crooked.florafatalis.care.domain.CareRecommendation;
import com.crooked.florafatalis.care.domain.CareType;
import com.crooked.florafatalis.care.domain.FertilizingCarePolicy;
import com.crooked.florafatalis.household.domain.HouseholdId;
import com.crooked.florafatalis.location.application.port.out.LocationRepository;
import com.crooked.florafatalis.location.domain.Location;
import com.crooked.florafatalis.location.domain.LocationId;
import com.crooked.florafatalis.photo.application.port.out.PlantPhotoRepository;
import com.crooked.florafatalis.photo.domain.PlantPhoto;
import com.crooked.florafatalis.plant.application.port.out.PlantRepository;
import com.crooked.florafatalis.plant.domain.Plant;
import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.application.port.out.ActiveHouseholdPort;
import com.crooked.florafatalis.shared.domain.UserId;
import com.crooked.florafatalis.species.application.port.out.SpeciesRepository;
import com.crooked.florafatalis.species.domain.Species;
import com.crooked.florafatalis.species.domain.SpeciesId;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
  private final FertilizingCarePolicy fertilizingCarePolicy;
  private final ActiveHouseholdPort activeHouseholdPort;
  private final PlantPhotoRepository plantPhotoRepository;
  private final LocationRepository locationRepository;
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

  private CareDashboard build(HouseholdId householdId, LocalDate today, ZoneId zone) {
    List<Plant> plants = plantRepository.findActiveByHousehold(householdId);
    Map<SpeciesId, Species> speciesById =
        speciesRepository.findAll().stream()
            .collect(Collectors.toMap(Species::id, Function.identity()));
    Map<PlantId, CareEvent> latestWatering = latestByPlant(householdId, CareType.WATERING);
    Map<PlantId, CareEvent> latestFertilizing = latestByPlant(householdId, CareType.FERTILIZING);
    Map<PlantId, String> primaryPhotoUrls = primaryPhotoUrls(householdId);
    Map<LocationId, String> locationNames =
        locationRepository.findByHousehold(householdId).stream()
            .collect(Collectors.toMap(Location::id, Location::name, (left, right) -> left));
    List<DashboardItem> overdue = new ArrayList<>();
    List<DashboardItem> dueToday = new ArrayList<>();
    List<DashboardItem> upcoming = new ArrayList<>();
    for (Plant plant : plants) {
      Species species = speciesById.get(plant.speciesId());
      if (species == null) {
        continue;
      }
      addItem(
          overdue,
          dueToday,
          upcoming,
          wateringItem(
              plant,
              species,
              latestWatering.get(plant.id()),
              today,
              zone,
              locationNames.get(plant.locationId()),
              primaryPhotoUrls.get(plant.id())),
          today);
      fertilizingCarePolicy
          .recommend(
              plant,
              species,
              Optional.ofNullable(latestFertilizing.get(plant.id())).map(CareEvent::performedAt),
              today,
              zone)
          .ifPresent(
              recommendation ->
                  addItem(
                      overdue,
                      dueToday,
                      upcoming,
                      item(
                          plant,
                          CareType.FERTILIZING,
                          recommendation,
                          today,
                          locationNames.get(plant.locationId()),
                          primaryPhotoUrls.get(plant.id())),
                      today));
    }
    overdue.sort(
        Comparator.comparing(DashboardItem::dueOn).thenComparing(DashboardItem::plantName));
    dueToday.sort(
        Comparator.comparing(DashboardItem::plantName).thenComparing(DashboardItem::careType));
    upcoming.sort(
        Comparator.comparing(DashboardItem::dueOn).thenComparing(DashboardItem::plantName));
    return new CareDashboard(overdue, dueToday, upcoming);
  }

  private DashboardItem wateringItem(
      Plant plant,
      Species species,
      CareEvent last,
      LocalDate today,
      ZoneId zone,
      String locationName,
      String primaryPhotoUrl) {
    Optional<Instant> lastAt = Optional.ofNullable(last).map(CareEvent::performedAt);
    CareRecommendation recommendation = carePolicy.recommend(plant, species, lastAt, today, zone);
    return item(plant, CareType.WATERING, recommendation, today, locationName, primaryPhotoUrl);
  }

  private DashboardItem item(
      Plant plant,
      CareType careType,
      CareRecommendation recommendation,
      LocalDate today,
      String locationName,
      String primaryPhotoUrl) {
    long days = ChronoUnit.DAYS.between(recommendation.dueOn(), today);
    return DashboardItem.of(
        plant.id(),
        plant.name(),
        locationName,
        careType.name(),
        recommendation.dueOn(),
        (int) Math.max(days, 0),
        primaryPhotoUrl);
  }

  private Map<PlantId, String> primaryPhotoUrls(HouseholdId householdId) {
    return plantPhotoRepository.findPrimaryByHouseholdId(householdId).stream()
        .collect(
            Collectors.toMap(
                PlantPhoto::plantId,
                photo -> "/api/photos/" + photo.id().value(),
                (left, right) -> left));
  }

  private void addItem(
      List<DashboardItem> overdue,
      List<DashboardItem> dueToday,
      List<DashboardItem> upcoming,
      DashboardItem item,
      LocalDate today) {
    if (item.dueOn().isBefore(today)) {
      overdue.add(item);
    } else if (item.dueOn().isEqual(today)) {
      dueToday.add(item);
    } else if (!item.dueOn().isAfter(today.plusDays(7))) {
      upcoming.add(item);
    }
  }

  private Map<PlantId, CareEvent> latestByPlant(HouseholdId householdId, CareType careType) {
    return careEventRepository.findLatestByHouseholdAndCareType(householdId, careType).stream()
        .collect(Collectors.toMap(CareEvent::plantId, Function.identity(), (left, right) -> left));
  }
}
