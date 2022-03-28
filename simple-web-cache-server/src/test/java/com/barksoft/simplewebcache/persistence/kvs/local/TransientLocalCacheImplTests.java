package com.barksoft.simplewebcache.persistence.kvs.local;

import com.barksoft.simplewebcache.persistence.kvs.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransientLocalCacheImplTests {
    private static final String DEFAULT_TEST_FID = "1,1a2b3c4d5";
    private static final String DEFAULT_TEST_FILE_NAME = "index.html";
    private static final StoreCachedIdRequest DEFAULT_STORE_CACHED_ID_REQUEST = StoreCachedIdRequest.builder()
            .fileStorageId(DEFAULT_TEST_FID)
            .fileName(DEFAULT_TEST_FILE_NAME)
            .build();
    private static final LoadCachedIdRequest DEFAULT_LOAD_CACHED_ID_REQUEST = LoadCachedIdRequest.builder()
            .resourceName(DEFAULT_TEST_FILE_NAME)
            .build();
    private KvsClient client;

    @BeforeEach
    public void setUp() {
        client = new TransientLocalCacheImpl();
    }

    @Test
    public void testStoreAndLoadCachedId() {
        StoreCachedIdResponse storeCachedIdResponse = client.storeCachedId(DEFAULT_STORE_CACHED_ID_REQUEST);
        assertThat(storeCachedIdResponse.getUpdated()).isTrue();
        LoadCachedIdResponse loadCachedIdResponse = client.loadCachedId(DEFAULT_LOAD_CACHED_ID_REQUEST);
        assertThat(loadCachedIdResponse.getId()).isPresent();
        assertThat(loadCachedIdResponse.getId().get()).isEqualTo(DEFAULT_TEST_FID);
    }

    @Test
    public void testLoadCachedIdNoIdStored() {
        LoadCachedIdResponse loadCachedIdResponse = client.loadCachedId(DEFAULT_LOAD_CACHED_ID_REQUEST);
        assertThat(loadCachedIdResponse.getId()).isNotPresent();
    }

    @Test
    public void testStoreCachedIdOverwrites() {
        String newFileId = "2,0p9o8i7u6y";
        StoreCachedIdResponse storeCachedIdResponse = client.storeCachedId(DEFAULT_STORE_CACHED_ID_REQUEST);
        assertThat(storeCachedIdResponse.getUpdated()).isTrue();
        StoreCachedIdRequest request = StoreCachedIdRequest.builder().fileName(DEFAULT_TEST_FILE_NAME).fileStorageId(newFileId).build();
        client.storeCachedId(request);
        LoadCachedIdResponse loadCachedIdResponse = client.loadCachedId(DEFAULT_LOAD_CACHED_ID_REQUEST);
        assertThat(loadCachedIdResponse.getId()).isPresent();
        assertThat(loadCachedIdResponse.getId()).get().isEqualTo(newFileId);
    }

    @Test
    public void testStoreCachedIdReturnsFalseIfValueIdentical() {
        StoreCachedIdResponse storeCachedIdResponse = client.storeCachedId(DEFAULT_STORE_CACHED_ID_REQUEST);
        assertThat(storeCachedIdResponse.getUpdated()).isTrue();
        StoreCachedIdResponse updatedStoreCachedIdResponse = client.storeCachedId(DEFAULT_STORE_CACHED_ID_REQUEST);
        assertThat(updatedStoreCachedIdResponse.getUpdated()).isFalse();
    }
}
