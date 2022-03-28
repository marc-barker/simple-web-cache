/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.file;

import com.barksoft.simplewebcache.seaweedfs.LoadFileRequest;
import com.barksoft.simplewebcache.seaweedfs.LoadFileResponse;
import com.barksoft.simplewebcache.seaweedfs.WriteFileRequest;
import com.barksoft.simplewebcache.seaweedfs.WriteFileResponse;

public interface FileStorageClient {
  WriteFileResponse writeFile(WriteFileRequest request);

  LoadFileResponse loadFile(LoadFileRequest request);
}
