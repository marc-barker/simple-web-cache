/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache;

import com.barksoft.simplewebcache.persistence.file.*;
import com.barksoft.simplewebcache.persistence.kvs.*;
import com.barksoft.simplewebcache.seaweedfs.LoadFileRequest;
import com.barksoft.simplewebcache.seaweedfs.LoadFileResponse;
import com.barksoft.simplewebcache.seaweedfs.WriteFileRequest;
import com.barksoft.simplewebcache.seaweedfs.WriteFileResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@EnableConfigurationProperties({FileStorageConfiguration.class, KvsConfiguration.class})
public class SimpleWebCacheApplication implements SimpleWebCache {
  private final FileStorageClient fileStorageClient;
  private final KvsClient kvsClient;
  private static final ReadWriteLockCache LOCK_CACHE = new ReadWriteLockCache();

  public static void main(String[] args) {
    SpringApplication.run(SimpleWebCacheApplication.class, args);
  }

  SimpleWebCacheApplication(FileStorageConfiguration fileStorageConfiguration, KvsConfiguration kvsConfiguration) {
    this.fileStorageClient = FileStorageServiceFactory.from(fileStorageConfiguration);
    this.kvsClient = KvsServiceFactory.from(kvsConfiguration);
  }

  @Override
  @PostMapping(value = "/getUrl", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public @ResponseBody Mono<ResponseEntity<byte[]>> getUrl(@RequestBody UrlRequest request) {
    URL parsedUrl = parseUrl(request.getUrl());
    String urlString = parsedUrl.toString();
    LOCK_CACHE.obtainReadLock(urlString);
    boolean holdsReadLock = true;
    try {
      LoadCachedIdRequest cachedIdRequest =
          LoadCachedIdRequest.builder().resourceName(urlString).build();
      LoadCachedIdResponse cachedIdResponse = kvsClient.loadCachedId(cachedIdRequest);

      if (cachedIdResponse.getId().isEmpty()) {
        LOCK_CACHE.releaseReadLock(urlString);
        holdsReadLock = false;
        LOCK_CACHE.obtainWriteLock(urlString);
        File tempFile = Files.createTempFile("", "").toFile();
        storeUrlContentsToFile(parsedUrl, tempFile);
        try {
          WriteFileRequest writeFileRequest =
              WriteFileRequest.builder().filePath(tempFile.getAbsolutePath()).build();
          WriteFileResponse writeFileResponse = fileStorageClient.writeFile(writeFileRequest);
          StoreCachedIdRequest storeCachedIdRequest =
              StoreCachedIdRequest.builder()
                  .fileName(urlString)
                  .fileStorageId(writeFileResponse.getStoredFileId())
                  .build();
          kvsClient.storeCachedId(storeCachedIdRequest);
        } finally {
          tempFile.delete();
          LOCK_CACHE.obtainReadLock(urlString);
          holdsReadLock = true;
          LOCK_CACHE.releaseWriteLock(urlString);
        }
      }
      LoadCachedIdResponse updatedCachedIdResponse = kvsClient.loadCachedId(cachedIdRequest);
      if (updatedCachedIdResponse.getId().isEmpty()) {
        throw new RuntimeException("Failed to store filename - file storage id mapping.");
      }
      LoadFileRequest loadFileRequest =
          LoadFileRequest.builder().fileId(updatedCachedIdResponse.getId().get()).build();
      LoadFileResponse getUrlResponse = fileStorageClient.loadFile(loadFileRequest);
      ByteBuffer resultContents = getUrlResponse.getContents();
      byte[] resultBytes = new byte[resultContents.capacity()];
      resultContents.get(resultBytes, 0, resultBytes.length);
      return Mono.just(
          ResponseEntity.ok()
              .header("Content-Type", getUrlResponse.getMimeType())
              .body(resultBytes));
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (RuntimeException re) {
      return Mono.just(
          ResponseEntity.internalServerError()
              .contentType(MediaType.TEXT_PLAIN)
              .body(re.getMessage().getBytes(StandardCharsets.UTF_8)));
    } finally {
      if (holdsReadLock) {
        LOCK_CACHE.releaseReadLock(urlString);
      }
      if (LOCK_CACHE.currentThreadHoldsWriteLock(urlString)) {
        LOCK_CACHE.releaseWriteLock(urlString);
      }
    }
  }

  @Override
  @PostMapping("getCachedFileId")
  public CachedFileIdResponse getCachedFileId(@RequestBody UrlRequest request) {
    String url = request.getUrl();
    LOCK_CACHE.obtainReadLock(url);
    try {
      LoadCachedIdResponse response =
              kvsClient.loadCachedId(
                      LoadCachedIdRequest.builder().resourceName(url).build());
      return CachedFileIdResponse.builder()
              .url(request.getUrl())
              .fileId(response.getId().orElse(""))
              .build();
    } finally {
      LOCK_CACHE.releaseReadLock(url);
    }
  }

  private void storeUrlContentsToFile(URL url, File file) {
    try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(file)) {
      fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private URL parseUrl(String rawUrl) {
    try {
      return new URL(rawUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to parse URL from string.", e);
    }
  }
}
