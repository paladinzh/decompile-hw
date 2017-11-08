package com.android.mms.util;

import android.content.Context;
import com.huawei.cspcommon.MLog;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LruSoftCache<K, V> {
    private static final String LOG_TAG = LruSoftCache.class.getSimpleName();
    private int createCount;
    private int evictionCount;
    private int hitCount;
    private final LinkedHashMap<K, SoftReference<V>> map;
    private int maxSize;
    private int missCount;
    private int putCount;
    private int size;

    public LruSoftCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap(0, 0.75f, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V get(Context context, K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        V mapReferent = null;
        synchronized (this) {
            SoftReference<V> mapValue = (SoftReference) this.map.get(key);
            if (mapValue != null) {
                mapReferent = mapValue.get();
            }
            if (mapReferent != null) {
                this.hitCount++;
                return mapReferent;
            }
            if (mapValue != null) {
                this.size -= safeSizeOf(key, null);
            }
            this.map.remove(key);
            this.missCount++;
        }
    }

    public final V put(K key, V referent) {
        if (key == null || referent == null) {
            throw new NullPointerException("key == null || value == null");
        }
        SoftReference<V> value = new SoftReference(referent);
        V previousReferent = null;
        synchronized (this) {
            this.putCount++;
            this.size += safeSizeOf(key, referent);
            SoftReference<V> previousValue = (SoftReference) this.map.put(key, value);
            if (previousValue != null) {
                previousReferent = previousValue.get();
                this.size -= safeSizeOf(key, previousReferent);
            }
        }
        if (previousValue != null) {
            entryRemoved(false, key, previousReferent, referent);
        }
        synchronized (this) {
            trimToSize(this.maxSize);
        }
        return previousReferent;
    }

    private void trimToSize(int maxSize) {
        while (true) {
            K key;
            Object obj;
            synchronized (this) {
                if (this.size >= 0 && (!this.map.isEmpty() || this.size == 0)) {
                    if (this.size <= maxSize || this.map.isEmpty()) {
                        break;
                    }
                    Entry<K, SoftReference<V>> toEvict = (Entry) this.map.entrySet().iterator().next();
                    key = toEvict.getKey();
                    SoftReference<V> value = (SoftReference) toEvict.getValue();
                    obj = value != null ? value.get() : null;
                    this.map.remove(key);
                    this.size -= safeSizeOf(key, obj);
                    this.evictionCount++;
                } else {
                    this.size = 0;
                    this.map.clear();
                    MLog.e(LOG_TAG, getClass().getName() + ".sizeOf() is reporting inconsistent results! size: " + this.size + ", maxSize: " + maxSize);
                }
            }
            entryRemoved(true, key, obj, null);
        }
    }

    public final V remove(K key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        V previousReferent = null;
        synchronized (this) {
            SoftReference<V> previousValue = (SoftReference) this.map.remove(key);
            if (previousValue != null) {
                previousReferent = previousValue.get();
                this.size -= safeSizeOf(key, previousReferent);
            }
        }
        if (previousValue != null) {
            entryRemoved(false, key, previousReferent, null);
        }
        return previousReferent;
    }

    protected void entryRemoved(boolean evicted, K k, V v, V v2) {
    }

    protected V create(Context context, K k) {
        return null;
    }

    private int safeSizeOf(K key, V value) {
        return sizeOf(key, value);
    }

    private final int sizeOf(K k, V v) {
        return 1;
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    public final synchronized int size() {
        return this.size;
    }

    public final synchronized String toString() {
        int hitPercent;
        int accesses = this.hitCount + this.missCount;
        hitPercent = accesses != 0 ? (this.hitCount * 100) / accesses : 0;
        return String.format("LruCache[size=%d,mapSize=%d,maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", new Object[]{Integer.valueOf(this.size), Integer.valueOf(this.map.size()), Integer.valueOf(this.maxSize), Integer.valueOf(this.hitCount), Integer.valueOf(this.missCount), Integer.valueOf(hitPercent)});
    }
}
