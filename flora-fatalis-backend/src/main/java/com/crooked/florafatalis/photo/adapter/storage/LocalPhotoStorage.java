package com.crooked.florafatalis.photo.adapter.storage;

import com.crooked.florafatalis.photo.application.port.out.PhotoStorage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalPhotoStorage implements PhotoStorage {

  private final Path root;

  public LocalPhotoStorage(@Value("${app.photos.dir:uploads}") String photosDir) {
    this.root = Path.of(photosDir).toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.root);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to create photos directory", ex);
    }
  }

  @Override
  public void store(String storageKey, byte[] content) {
    try {
      Path path = resolve(storageKey);
      Files.createDirectories(path.getParent());
      Files.write(path, content);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to store photo", ex);
    }
  }

  @Override
  public byte[] load(String storageKey) {
    try {
      return Files.readAllBytes(resolve(storageKey));
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to load photo", ex);
    }
  }

  @Override
  public void delete(String storageKey) {
    try {
      Files.deleteIfExists(resolve(storageKey));
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to delete photo", ex);
    }
  }

  private Path resolve(String storageKey) {
    Path path = root.resolve(storageKey).normalize();
    if (!path.startsWith(root)) {
      throw new IllegalArgumentException("invalid storage key");
    }
    return path;
  }
}
