package com.avast.android.sdk.engine.obfuscated;

import com.huawei.systemmanager.optimize.base.Const;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class ap<K, V> {
    private final LinkedHashMap<K, V> a;
    private int b;
    private int c;
    private int d;
    private int e;
    private int f;
    private int g;
    private int h;

    public ap(int i) {
        if (i > 0) {
            this.c = i;
            this.a = new LinkedHashMap(0, Const.FREE_MEMORY_RISK_FLOAT, true);
            return;
        }
        throw new IllegalArgumentException("maxSize <= 0");
    }

    private void a(int i) {
        while (true) {
            Object key;
            Object value;
            synchronized (this) {
                if (this.b < 0) {
                    break;
                }
                if (this.a.isEmpty()) {
                    if (this.b != 0) {
                        break;
                    }
                }
                if (this.b > i) {
                    Entry entry = null;
                    for (Entry entry2 : this.a.entrySet()) {
                    }
                    if (entry != null) {
                        key = entry.getKey();
                        value = entry.getValue();
                        this.a.remove(key);
                        this.b -= c(key, value);
                        this.f++;
                    } else {
                        return;
                    }
                }
                return;
            }
            a(true, key, value, null);
        }
        throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
    }

    private int c(K k, V v) {
        int b = b(k, v);
        if (b >= 0) {
            return b;
        }
        throw new IllegalStateException("Negative size: " + k + "=" + v);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V a(K k) {
        if (k != null) {
            synchronized (this) {
                V v = this.a.get(k);
                if (v == null) {
                    this.h++;
                } else {
                    this.g++;
                    return v;
                }
            }
        }
        throw new NullPointerException("key == null");
    }

    public final V a(K k, V v) {
        if (k == null || v == null) {
            throw new NullPointerException("key == null || value == null");
        }
        V put;
        synchronized (this) {
            this.d++;
            this.b += c(k, v);
            put = this.a.put(k, v);
            if (put != null) {
                this.b -= c(k, put);
            }
        }
        if (put != null) {
            a(false, k, put, v);
        }
        a(this.c);
        return put;
    }

    public final void a() {
        a(-1);
    }

    protected void a(boolean z, K k, V v, V v2) {
    }

    protected int b(K k, V v) {
        return 1;
    }

    protected V b(K k) {
        return null;
    }

    public final synchronized String toString() {
        String format;
        int i = 0;
        synchronized (this) {
            int i2 = this.g + this.h;
            if (i2 != 0) {
                i = (this.g * 100) / i2;
            }
            format = String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", new Object[]{Integer.valueOf(this.c), Integer.valueOf(this.g), Integer.valueOf(this.h), Integer.valueOf(i)});
        }
        return format;
    }
}
