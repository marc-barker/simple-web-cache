/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache.persistence.file.seaweed;

import com.barksoft.simplewebcache.persistence.file.*;
import com.barksoft.simplewebcache.persistence.file.seaweedfs.*;
import com.barksoft.simplewebcache.seaweedfs.*;
import com.barksoft.simplewebcache.seaweedfs.LoadFileResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Objects;
import okhttp3.*;
import org.apache.tika.Tika;

public class SeaweedFSClientImpl implements SeaweedFSClient, FileStorageClient {
  private static final String ALLOCATE_FILE_STORAGE_API = "/dir/assign";
  private static final String VOLUME_LOCATION_LOOKUP_API = "/dir/lookup?volumeId=";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final OkHttpClient client;
  private final String seaweedServerUrl;

  public SeaweedFSClientImpl(String seaweedServerUrl) {
    this.client = new OkHttpClient();
    // Strip trailing slash if it exists
    if (seaweedServerUrl.endsWith("/")) {
      this.seaweedServerUrl = seaweedServerUrl.substring(0, seaweedServerUrl.length() - 1);
    } else {
      this.seaweedServerUrl = seaweedServerUrl;
    }
  }

  @Override
  public AllocateFileStorageResponse allocateFileStorage() {
    URL url = getUrlForEndpoint(ALLOCATE_FILE_STORAGE_API);
    return executeGetRequestAndMapResult(url, AllocateFileStorageResponse.class);
  }

  @Override
  public StoreFileResponse storeFile(StoreFileRequest request) {
    try {
      Tika tika = new Tika();
      File file = new File(request.getFilePath());
      String mimeType = tika.detect(file);
      RequestBody body =
          new MultipartBody.Builder()
              .setType(MultipartBody.FORM)
              .addFormDataPart(
                  "file",
                  request.getFilePath(),
                  RequestBody.create(MediaType.get(mimeType), file))
              .build();
      String storageUrlWithProtocol = String.join("", "http://", request.getStorageUrl());
      return executePostRequestAndMapResult(getUrlFromUrlAndEndpoint(storageUrlWithProtocol, request.getFileId()), body, StoreFileResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public GetVolumeLocationResponse getVolumeLocation(GetVolumeLocationRequest request) {
    String endpointWithParameters =
        String.join("", VOLUME_LOCATION_LOOKUP_API, String.valueOf(request.getVolumeId()));
    URL url = getUrlForEndpoint(endpointWithParameters);
    return executeGetRequestAndMapResult(url, GetVolumeLocationResponse.class);
  }

  @Override
  public LoadFileResponse getFile(GetFileRequest request) {
    URL requestUrl =
        new HttpUrl.Builder()
            .scheme("http")
            .host(request.getVolumeHost())
            .port(request.getVolumePort())
            .addPathSegment(request.getFileId())
            .build()
            .url();
    Response response = executeGetRequest(requestUrl);
    MediaType mediaType = MediaType.get(Objects.requireNonNull(response.header("Content-Type")));
    try {
      return LoadFileResponse.builder()
          .mimeType(mediaType.toString())
          .contents(ByteBuffer.wrap(Objects.requireNonNull(response.body()).bytes()))
          .build();
    } catch (IOException e) {
      throw new RuntimeException("Unable to get file contents.", e);
    }
  }

  @Override
  public WriteFileResponse writeFile(WriteFileRequest request) {
    AllocateFileStorageResponse allocateFileStorageResponse = allocateFileStorage();
    StoreFileRequest storeFileRequest =
        StoreFileRequest.builder()
            .fileId(allocateFileStorageResponse.getFid())
            .filePath(request.getFilePath())
            .storageUrl(allocateFileStorage().getUrl())
            .build();
    StoreFileResponse storeFileResponse = storeFile(storeFileRequest);
    System.out.println("File stored.");
    return WriteFileResponse.builder()
        .storedFileName(storeFileResponse.getName())
        .storedFileId(allocateFileStorageResponse.getFid())
        .build();
  }

  @Override
  public LoadFileResponse loadFile(LoadFileRequest request) {
    String volumeId = request.getFileId().substring(0, request.getFileId().indexOf(","));
    GetVolumeLocationRequest getVolumeLocationRequest =
        GetVolumeLocationRequest.builder().volumeId(Integer.parseInt(volumeId)).build();
    GetVolumeLocationResponse getVolumeLocationResponse =
        getVolumeLocation(getVolumeLocationRequest);
    Location firstLocation = getVolumeLocationResponse.getLocations().get(0);
    if (firstLocation == null) {
      throw new RuntimeException("No volume located for file.");
    }
    String locationUrl = firstLocation.getUrl();
    int delimeterIdx = locationUrl.indexOf(":");
    String host = locationUrl.substring(0, delimeterIdx);
    Integer port = Integer.valueOf(locationUrl.substring(delimeterIdx + 1));
    GetFileRequest getFileRequest =
        GetFileRequest.builder()
            .volumeHost(host)
            .volumePort(port)
            .fileId(request.getFileId())
            .build();
    return getFile(getFileRequest);
  }

  private URL getUrlForEndpoint(String apiEndpoint) {
    return getUrlFromUrlAndEndpoint(seaweedServerUrl, apiEndpoint);
  }

  private URL getUrlFromUrlAndEndpoint(String url, String apiEndpoint) {
    String combinedUrl = String.join("/", url, apiEndpoint);
    try {
      return new URL(combinedUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to parse valid URL from string.", e);
    }
  }

  private <T> T executeGetRequestAndMapResult(URL requestUrl, Class<T> returnType) {
    Response response = executeGetRequest(requestUrl);
    return mapResponseToReturnType(response, returnType);
  }

  private <T> T executePostRequestAndMapResult(URL requestUrl, RequestBody body, Class<T> returnType) {
    Response response = executePostRequest(requestUrl, body);
    System.out.println(response);
    return mapResponseToReturnType(response, returnType);
  }

  private <T> T mapResponseToReturnType(Response response, Class<T> returnType) {
    try {
      String responseString = response.body().string();
      System.out.println(responseString);
      T result = MAPPER.readValue(responseString, returnType);
      return result;
    } catch (IOException e) {
        throw new RuntimeException("Unable to parse response into return type.", e);
    }
  }

  private Response executeGetRequest(URL requestUrl) {
    Request request = new Request.Builder().url(requestUrl).build();
    return executeRequest(request);
  }

  private Response executePostRequest(URL requestUrl, RequestBody body) {
    Request request = new Request.Builder().url(requestUrl).post(body).build();
    return executeRequest(request);
  }

  private Response executeRequest(Request request) {
    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      throw new RuntimeException("Unable to execute http request.", e);
    }
  }
}
