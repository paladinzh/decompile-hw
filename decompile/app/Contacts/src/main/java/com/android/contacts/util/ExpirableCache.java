package com.android.contacts.util;

import android.util.LruCache;
import com.android.contacts.test.NeededForTesting;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class ExpirableCache<K, V> {
    private LruCache<K, CachedValue<V>> mCache;
    private final AtomicInteger mGeneration = new AtomicInteger(0);

    public interface CachedValue<V> {
        V getValue();

        boolean isExpired();
    }

    @Immutable
    private static class GenerationalCachedValue<V> implements CachedValue<V> {
        private final AtomicInteger mCacheGeneration;
        private final int mGeneration = this.mCacheGeneration.get();
        public final V mValue;

        public GenerationalCachedValue(V value, AtomicInteger cacheGeneration) {
            this.mValue = value;
            this.mCacheGeneration = cacheGeneration;
        }

        public V getValue() {
            return this.mValue;
        }

        public boolean isExpired() {
            return this.mGeneration != this.mCacheGeneration.get();
        }
    }

    private ExpirableCache(LruCache<K, CachedValue<V>> cache) {
        this.mCache = cache;
    }

    public CachedValue<V> getCachedValue(K key) {
        return (CachedValue) this.mCache.get(key);
    }

    public V getPossiblyExpired(K key) {
        CachedValue<V> cachedValue = getCachedValue(key);
        if (cachedValue == null) {
            return null;
        }
        return cachedValue.getValue();
    }

    @NeededForTesting
    public V get(K key) {
        CachedValue<V> cachedValue = getCachedValue(key);
        if (cachedValue == null || cachedValue.isExpired()) {
            return null;
        }
        return cachedValue.getValue();
    }

    public void put(K key, V value) {
        this.mCache.put(key, newCachedValue(value));
    }

    public void expireAll() {
        this.mGeneration.incrementAndGet();
    }

    public CachedValue<V> newCachedValue(V value) {
        return new GenerationalCachedValue(value, this.mGeneration);
    }

    public static <K, V> ExpirableCache<K, V> create(LruCache<K, CachedValue<V>> cache) {
        return new ExpirableCache(cache);
    }

    public static <K, V> ExpirableCache<K, V> create(int maxSize) {
        return create(new LruCache(maxSize));
    }
}
