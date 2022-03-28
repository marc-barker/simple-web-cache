/* Copyright (c) 2022 Marc Barker - MIT License */
package com.barksoft.simplewebcache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import okhttp3.*;

public class GetUrlTestClient {
  public static void main(String[] args) throws IOException {
    OkHttpClient client = new OkHttpClient();
    ObjectMapper mapper = new ObjectMapper();
    UrlRequest getUrlRequest =
        UrlRequest.builder()
            .url("https://www.basketball-reference.com/players/a/adelde01.html")
            .build();
    System.out.println(mapper.writeValueAsString(getUrlRequest));
    RequestBody requestBody =
        RequestBody.create(MediaType.get("application/json"), mapper.writeValueAsString(getUrlRequest));
    Request request =
        new Request.Builder().url("http://localhost:8081/getUrl/").post(requestBody).build();
    try {
      Response response = client.newCall(request).execute();
      System.out.println(response.headers());
      Files.write(Path.of("./tempfile"), Objects.requireNonNull(response.body()).bytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
