package com.crooked.florafatalis.care.application.port.in;

import com.crooked.florafatalis.plant.domain.PlantId;
import com.crooked.florafatalis.shared.domain.UserId;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetCareDashboardUseCase {

  CareDashboard get(UserId userId);

  record CareDashboard(
      List<DashboardItem> overdue, List<DashboardItem> dueToday, List<DashboardItem> upcoming) {}

  record DashboardItem(
      UUID plantId,
      String plantName,
      String careType,
      LocalDate dueOn,
      int overdueDays,
      String primaryPhotoUrl) {

    public static DashboardItem of(
        PlantId plantId,
        String plantName,
        String careType,
        LocalDate dueOn,
        int overdueDays,
        String primaryPhotoUrl) {
      return new DashboardItem(
          plantId.value(), plantName, careType, dueOn, overdueDays, primaryPhotoUrl);
    }
  }
}
