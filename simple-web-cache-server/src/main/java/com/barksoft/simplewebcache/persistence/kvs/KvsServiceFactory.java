package com.barksoft.simplewebcache.persistence.kvs;

import com.barksoft.simplewebcache.persistence.kvs.local.TransientLocalCacheImpl;

public final class KvsServiceFactory {
    public static KvsClient from(KvsConfiguration configuration) {
        if (configuration.backend().equals(KvsBackend.LOCAL)) {
            return new TransientLocalCacheImpl();
        }
        throw new RuntimeException("Invalid key-value store backend specified in configuration!");
    }
}
