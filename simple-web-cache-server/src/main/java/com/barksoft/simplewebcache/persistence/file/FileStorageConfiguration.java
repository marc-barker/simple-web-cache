/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.file;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Spring configuration for the file storage backend.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "file-storage")
public final class FileStorageConfiguration {
  private final FileStorageBackend backend;
  private final String protocol;
  private final String hostname;
  private final Integer port;

  public FileStorageConfiguration(
      FileStorageBackend backend, String protocol, String hostname, Integer port) {
    this.backend = backend;
    this.protocol = protocol;
    this.hostname = hostname;
    this.port = port;
  }

  public FileStorageBackend backend() {
    return backend;
  }

  public String protocol() {
    return protocol;
  }

  public String hostname() {
    return hostname;
  }

  public Integer port() {
    return port;
  }
}
