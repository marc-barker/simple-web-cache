/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.kvs;

public interface KvsClient {
  LoadCachedIdResponse loadCachedId(LoadCachedIdRequest request);

  StoreCachedIdResponse storeCachedId(StoreCachedIdRequest request);
}
