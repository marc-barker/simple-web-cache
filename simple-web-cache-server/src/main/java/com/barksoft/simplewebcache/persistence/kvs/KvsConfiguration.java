package com.barksoft.simplewebcache.persistence.kvs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Spring configuration for the key-value store backend.
 */
@ConstructorBinding
@ConfigurationProperties(prefix = "kvs")
public final class KvsConfiguration {
    private final KvsBackend backend;

    public KvsConfiguration(KvsBackend backend) {
        this.backend = backend;
    }

    public KvsBackend backend() {
        return backend;
    }
}
