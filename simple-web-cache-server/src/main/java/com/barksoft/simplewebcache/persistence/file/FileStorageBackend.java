/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.file;

/**
 * Set of implementations of {@link FileStorageClient}, used in config to determine which file storage service to write to.
 */
public enum FileStorageBackend {
  SEAWEEDFS
}
