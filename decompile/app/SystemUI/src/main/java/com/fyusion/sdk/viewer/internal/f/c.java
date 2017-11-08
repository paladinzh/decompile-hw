package com.fyusion.sdk.viewer.internal.f;

import android.support.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* compiled from: Unknown */
public class c<T, Y> {
    private final LinkedHashMap<T, Y> a = new LinkedHashMap(100, 0.75f, true);
    private final int b;
    private int c;
    private int d = 0;

    public c(int i) {
        this.b = i;
        this.c = i;
    }

    private void a() {
        a(this.c);
    }

    protected int a(Y y) {
        return 1;
    }

    protected synchronized void a(int i) {
        while (this.d > i) {
            Entry entry = (Entry) this.a.entrySet().iterator().next();
            Object value = entry.getValue();
            this.d -= a(value);
            Object key = entry.getKey();
            this.a.remove(key);
            a(key, value);
        }
    }

    protected void a(T t, Y y) {
    }

    @Nullable
    public synchronized Y b(T t) {
        return this.a.get(t);
    }

    public synchronized Y b(T t, Y y) {
        if (a((Object) y) < this.c) {
            Object put = this.a.put(t, y);
            if (y != null) {
                this.d += a((Object) y);
            }
            if (put != null) {
                this.d -= a(put);
            }
            a();
            return put;
        }
        a(t, y);
        return null;
    }

    @Nullable
    public synchronized Y c(T t) {
        Object remove;
        remove = this.a.remove(t);
        if (remove != null) {
            this.d -= a(remove);
        }
        return remove;
    }
}
