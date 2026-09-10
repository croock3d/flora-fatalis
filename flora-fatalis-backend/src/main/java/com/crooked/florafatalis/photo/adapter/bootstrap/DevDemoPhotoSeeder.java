package com.crooked.florafatalis.photo.adapter.bootstrap;

import com.crooked.florafatalis.photo.adapter.repository.jpa.PlantPhotoJpaRepository;
import com.crooked.florafatalis.photo.adapter.repository.jpa.entity.PlantPhotoEntity;
import com.crooked.florafatalis.photo.application.port.out.PhotoStorage;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.photos.seed-demo", havingValue = "true")
public class DevDemoPhotoSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DevDemoPhotoSeeder.class);

  private final PlantPhotoJpaRepository plantPhotoJpaRepository;
  private final PhotoStorage photoStorage;

  @Override
  public void run(ApplicationArguments args) {
    int copied = 0;
    for (PlantPhotoEntity photo : plantPhotoJpaRepository.findAll()) {
      ClassPathResource resource = new ClassPathResource("dev-photos/" + photo.getId() + ".jpg");
      if (!resource.exists()) {
        continue;
      }
      try (InputStream input = resource.getInputStream()) {
        photoStorage.store(photo.getStorageKey(), input.readAllBytes());
        copied++;
      } catch (IOException ex) {
        throw new IllegalStateException("Failed to seed demo photo " + photo.getId(), ex);
      }
    }
    if (copied > 0) {
      log.info("DevDemoPhotoSeeder: copied {} demo photos to local storage", copied);
    }
  }
}
