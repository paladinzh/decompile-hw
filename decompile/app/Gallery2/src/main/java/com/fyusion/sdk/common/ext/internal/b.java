package com.fyusion.sdk.common.ext.internal;

import com.fyusion.sdk.common.ext.Key;
import java.util.List;

/* compiled from: Unknown */
public abstract class b {
    private Settings a;

    /* compiled from: Unknown */
    public static class a {
        protected Settings a = new Settings();

        public <T> void set(Key<T> key, T t) {
            this.a.set(key, t);
        }
    }

    protected b(Settings settings) {
        this.a = settings;
    }

    public <T> T get(Key<T> key) {
        return this.a.get(key);
    }

    public List<Key> getKeys() {
        return this.a.getKeys();
    }
}
