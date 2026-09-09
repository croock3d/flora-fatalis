package com.crooked.florafatalis.photo.application.port.out;

public interface PhotoStorage {

  void store(String storageKey, byte[] content);

  byte[] load(String storageKey);

  void delete(String storageKey);
}
