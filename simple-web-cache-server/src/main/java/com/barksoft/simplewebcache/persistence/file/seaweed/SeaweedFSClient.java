/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.file.seaweed;

import com.barksoft.simplewebcache.persistence.file.seaweedfs.*;
import com.barksoft.simplewebcache.seaweedfs.LoadFileResponse;

public interface SeaweedFSClient {
  AllocateFileStorageResponse allocateFileStorage();

  StoreFileResponse storeFile(StoreFileRequest request);

  GetVolumeLocationResponse getVolumeLocation(GetVolumeLocationRequest request);

  LoadFileResponse getFile(GetFileRequest request);
}
