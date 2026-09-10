package com.crooked.florafatalis.care.adapter.config;

import com.crooked.florafatalis.care.domain.FertilizingCarePolicy;
import com.crooked.florafatalis.care.domain.IntervalCarePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CareConfig {

  @Bean
  IntervalCarePolicy intervalCarePolicy() {
    return new IntervalCarePolicy();
  }

  @Bean
  FertilizingCarePolicy fertilizingCarePolicy() {
    return new FertilizingCarePolicy();
  }
}
