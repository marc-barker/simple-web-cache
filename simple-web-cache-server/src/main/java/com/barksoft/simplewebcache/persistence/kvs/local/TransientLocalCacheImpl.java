/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.kvs.local;

import com.barksoft.simplewebcache.persistence.kvs.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class TransientLocalCacheImpl implements KvsClient {
  private final Map<String, String> cache;

  public TransientLocalCacheImpl() {
    this.cache = new ConcurrentHashMap<>();
  }

  @Override
  public LoadCachedIdResponse loadCachedId(LoadCachedIdRequest request) {
    String storedValue = cache.get(request.getResourceName());
    return LoadCachedIdResponse.builder().id(Optional.ofNullable(storedValue)).build();
  }

  @Override
  public StoreCachedIdResponse storeCachedId(StoreCachedIdRequest request) {
    String previousValue = cache.put(request.getFileName(), request.getFileStorageId());
    boolean valueUpdated = previousValue == null || !previousValue.equals(request.getFileStorageId());
    return StoreCachedIdResponse.builder().updated(valueUpdated).build();
  }
}
