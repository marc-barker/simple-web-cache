package com.barksoft.simplewebcache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReadWriteLockCache {
    private static final LoadingCache<String, ReentrantReadWriteLock> LOCK_CACHE =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(Duration.ofSeconds(10))
                    .build(
                            new CacheLoader<>() {
                                @Override
                                public ReentrantReadWriteLock load(String key) {
                                    return new ReentrantReadWriteLock();
                                }
                            });

    public ReadWriteLockCache() {}

    void obtainReadLock(String lockDescriptor) {
        try {
            LOCK_CACHE.getUnchecked(lockDescriptor).readLock().tryLock(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void releaseReadLock(String lockDescriptor) {
        LOCK_CACHE.getUnchecked(lockDescriptor).readLock().unlock();
    }

    void obtainWriteLock(String lockDescriptor) {
        try {
            LOCK_CACHE.getUnchecked(lockDescriptor).writeLock().tryLock(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void releaseWriteLock(String lockDescriptor) {
        LOCK_CACHE.getUnchecked(lockDescriptor).writeLock().unlock();
    }

    Boolean currentThreadHoldsWriteLock(String lockDescriptor) {
        return LOCK_CACHE.getUnchecked(lockDescriptor).isWriteLockedByCurrentThread();
    }
}
