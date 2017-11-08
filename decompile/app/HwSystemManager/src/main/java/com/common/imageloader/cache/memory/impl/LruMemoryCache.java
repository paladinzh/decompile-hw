package com.common.imageloader.cache.memory.impl;

import android.graphics.Bitmap;
import com.common.imageloader.cache.memory.MemoryCache;
import com.huawei.systemmanager.optimize.base.Const;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LruMemoryCache implements MemoryCache {
    private final LinkedHashMap<String, Bitmap> map;
    private final int maxSize;
    private int size;

    public LruMemoryCache(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.maxSize = maxSize;
        this.map = new LinkedHashMap(0, Const.FREE_MEMORY_RISK_FLOAT, true);
    }

    public final Bitmap get(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        Bitmap bitmap;
        synchronized (this) {
            bitmap = (Bitmap) this.map.get(key);
        }
        return bitmap;
    }

    public final boolean put(String key, Bitmap value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        synchronized (this) {
            this.size += sizeOf(key, value);
            Bitmap previous = (Bitmap) this.map.put(key, value);
            if (previous != null) {
                this.size -= sizeOf(key, previous);
            }
        }
        trimToSize(this.maxSize);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void trimToSize(int maxSize) {
        while (true) {
            synchronized (this) {
                if (this.size >= 0 && (!this.map.isEmpty() || this.size == 0)) {
                    if (this.size <= maxSize || this.map.isEmpty()) {
                        break;
                    }
                    Entry<String, Bitmap> toEvict = (Entry) this.map.entrySet().iterator().next();
                    if (toEvict == null) {
                        break;
                    }
                    String key = (String) toEvict.getKey();
                    Bitmap value = (Bitmap) toEvict.getValue();
                    this.map.remove(key);
                    this.size -= sizeOf(key, value);
                }
            }
        }
    }

    public final Bitmap remove(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        Bitmap previous;
        synchronized (this) {
            previous = (Bitmap) this.map.remove(key);
            if (previous != null) {
                this.size -= sizeOf(key, previous);
            }
        }
        return previous;
    }

    public Collection<String> keys() {
        Collection hashSet;
        synchronized (this) {
            hashSet = new HashSet(this.map.keySet());
        }
        return hashSet;
    }

    public void clear() {
        trimToSize(-1);
    }

    private int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    public final synchronized String toString() {
        return String.format("LruCache[maxSize=%d]", new Object[]{Integer.valueOf(this.maxSize)});
    }
}
