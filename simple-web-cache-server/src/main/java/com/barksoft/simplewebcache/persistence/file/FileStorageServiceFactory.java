/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.file;

import com.barksoft.simplewebcache.persistence.file.seaweed.SeaweedFSClientImpl;
import java.net.URL;
import okhttp3.HttpUrl;

public final class FileStorageServiceFactory {
  public static FileStorageClient from(FileStorageConfiguration configuration) {
    if (configuration.backend().equals(FileStorageBackend.SEAWEEDFS)) {
      URL seaweedUrl =
          new HttpUrl.Builder()
              .scheme(configuration.protocol())
              .host(configuration.hostname())
              .port(configuration.port())
              .build()
              .url();
      return new SeaweedFSClientImpl(seaweedUrl.toString());
    }
    throw new RuntimeException("Unsupported file storage config!");
  }
}
