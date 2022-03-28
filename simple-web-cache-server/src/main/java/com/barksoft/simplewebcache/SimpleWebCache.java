/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache;

import com.barksoft.simplewebcache.CachedFileIdResponse;
import com.barksoft.simplewebcache.UrlRequest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface SimpleWebCache {
  Mono<ResponseEntity<byte[]>> getUrl(UrlRequest url);

  CachedFileIdResponse getCachedFileId(UrlRequest url);
}
