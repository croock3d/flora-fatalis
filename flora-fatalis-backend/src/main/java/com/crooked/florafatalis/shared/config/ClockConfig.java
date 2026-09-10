package com.crooked.florafatalis.shared.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ClockConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  ZoneId appZoneId(@Value("${app.timezone:Europe/Warsaw}") String timezone) {
    return ZoneId.of(timezone);
  }
}
