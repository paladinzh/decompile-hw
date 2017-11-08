package com.google.android.gms.internal;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class ev<K, V> {
    private final LinkedHashMap<K, V> Ch;
    private int Ci;
    private int Cj;
    private int Cl;
    private int Cm;
    private int Cn;
    private int size;

    private int c(K k, V v) {
        int sizeOf = sizeOf(k, v);
        if (sizeOf >= 0) {
            return sizeOf;
        }
        throw new IllegalStateException("Negative size: " + k + "=" + v);
    }

    protected void entryRemoved(boolean evicted, K k, V v, V v2) {
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        V put;
        synchronized (this) {
            this.Cj++;
            this.size += c(key, value);
            put = this.Ch.put(key, value);
            if (put != null) {
                this.size -= c(key, put);
            }
        }
        if (put != null) {
            entryRemoved(false, key, put, value);
        }
        trimToSize(this.Ci);
        return put;
    }

    protected int sizeOf(K k, V v) {
        return 1;
    }

    public final synchronized String toString() {
        String format;
        int i = 0;
        synchronized (this) {
            int i2 = this.Cm + this.Cn;
            if (i2 != 0) {
                i = (this.Cm * 100) / i2;
            }
            format = String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", new Object[]{Integer.valueOf(this.Ci), Integer.valueOf(this.Cm), Integer.valueOf(this.Cn), Integer.valueOf(i)});
        }
        return format;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void trimToSize(int maxSize) {
        while (true) {
            Object key;
            Object value;
            synchronized (this) {
                if (this.size < 0) {
                    break;
                }
                if (this.Ch.isEmpty()) {
                    if (this.size != 0) {
                        break;
                    }
                }
                if (this.size > maxSize && !this.Ch.isEmpty()) {
                    Entry entry = (Entry) this.Ch.entrySet().iterator().next();
                    key = entry.getKey();
                    value = entry.getValue();
                    this.Ch.remove(key);
                    this.size -= c(key, value);
                    this.Cl++;
                }
            }
            entryRemoved(true, key, value, null);
        }
        throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
    }
}
