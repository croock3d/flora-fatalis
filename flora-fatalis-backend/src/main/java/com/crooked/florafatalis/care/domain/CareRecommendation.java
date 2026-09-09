package com.crooked.florafatalis.care.domain;

import java.time.LocalDate;

public record CareRecommendation(LocalDate dueOn, int intervalDays) {}
