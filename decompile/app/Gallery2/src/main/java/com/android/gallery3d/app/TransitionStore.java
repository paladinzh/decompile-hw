package com.android.gallery3d.app;

import java.util.HashMap;

public class TransitionStore {
    private HashMap<Object, Object> mStorage = new HashMap();

    public void put(Object key, Object value) {
        this.mStorage.put(key, value);
    }

    public <T> T get(Object key) {
        return this.mStorage.get(key);
    }

    public <T> T get(Object key, T valueIfNull) {
        T value = this.mStorage.get(key);
        return value == null ? valueIfNull : value;
    }

    public void clear() {
        this.mStorage.clear();
    }
}
