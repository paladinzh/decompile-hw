package com.google.android.gms.internal;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class zzlf<K, V> {
    private int size;
    private int zzaeA;
    private int zzaeB;
    private int zzaeC;
    private int zzaeD;
    private int zzaeE;
    private int zzaeF;
    private final LinkedHashMap<K, V> zzaez;

    public zzlf(int i) {
        if (i > 0) {
            this.zzaeA = i;
            this.zzaez = new LinkedHashMap(0, 0.75f, true);
            return;
        }
        throw new IllegalArgumentException("maxSize <= 0");
    }

    private int zzc(K k, V v) {
        int sizeOf = sizeOf(k, v);
        if (sizeOf >= 0) {
            return sizeOf;
        }
        throw new IllegalStateException("Negative size: " + k + "=" + v);
    }

    protected V create(K k) {
        return null;
    }

    protected void entryRemoved(boolean evicted, K k, V v, V v2) {
    }

    public final void evictAll() {
        trimToSize(-1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V get(K key) {
        if (key != null) {
            synchronized (this) {
                V v = this.zzaez.get(key);
                if (v == null) {
                    this.zzaeF++;
                } else {
                    this.zzaeE++;
                    return v;
                }
            }
        }
        throw new NullPointerException("key == null");
    }

    public final V put(K key, V value) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }
        V put;
        synchronized (this) {
            this.zzaeB++;
            this.size += zzc(key, value);
            put = this.zzaez.put(key, value);
            if (put != null) {
                this.size -= zzc(key, put);
            }
        }
        if (put != null) {
            entryRemoved(false, key, put, value);
        }
        trimToSize(this.zzaeA);
        return put;
    }

    protected int sizeOf(K k, V v) {
        return 1;
    }

    public final synchronized String toString() {
        String format;
        int i = 0;
        synchronized (this) {
            int i2 = this.zzaeE + this.zzaeF;
            if (i2 != 0) {
                i = (this.zzaeE * 100) / i2;
            }
            format = String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", new Object[]{Integer.valueOf(this.zzaeA), Integer.valueOf(this.zzaeE), Integer.valueOf(this.zzaeF), Integer.valueOf(i)});
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
                if (this.zzaez.isEmpty()) {
                    if (this.size != 0) {
                        break;
                    }
                }
                if (this.size > maxSize && !this.zzaez.isEmpty()) {
                    Entry entry = (Entry) this.zzaez.entrySet().iterator().next();
                    key = entry.getKey();
                    value = entry.getValue();
                    this.zzaez.remove(key);
                    this.size -= zzc(key, value);
                    this.zzaeD++;
                }
            }
            entryRemoved(true, key, value, null);
        }
        throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
    }
}
