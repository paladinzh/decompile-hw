package com.loc;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* compiled from: SimpleLruCache */
public class cn<K, V> {
    private final LinkedHashMap<K, V> a;
    private int b;
    private int c;

    public cn(int i) {
        if (i > 0) {
            this.c = i;
            this.a = new LinkedHashMap(0, 0.75f, true);
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
        int a = a(k, v);
        if (a >= 0) {
            return a;
        }
        throw new IllegalStateException("Negative size: " + k + "=" + v);
    }

    protected int a(K k, V v) {
        return 1;
    }

    protected V a(K k) {
        return null;
    }

    public final void a() {
        a(-1);
    }

    protected void a(boolean z, K k, V v, V v2) {
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final V b(K k) {
        if (k != null) {
            synchronized (this) {
                V v = this.a.get(k);
                if (v == null) {
                } else {
                    return v;
                }
            }
        }
        throw new NullPointerException("key == null");
    }

    public final V b(K k, V v) {
        if (k == null || v == null) {
            throw new NullPointerException("key == null || value == null");
        }
        V put;
        synchronized (this) {
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

    public final V c(K k) {
        if (k != null) {
            V remove;
            synchronized (this) {
                remove = this.a.remove(k);
                if (remove != null) {
                    this.b -= c(k, remove);
                }
            }
            if (remove != null) {
                a(false, k, remove, null);
            }
            return remove;
        }
        throw new NullPointerException("key == null");
    }
}
