package com.fyusion.sdk.common.ext.internal;

import com.fyusion.sdk.common.ext.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class Settings {
    private Map<Key, Object> a = new HashMap();

    public <T> T get(Key<T> key) {
        T t = this.a.get(key);
        return t == null ? null : t;
    }

    public List<Key> getKeys() {
        List<Key> arrayList = new ArrayList();
        for (Key add : this.a.keySet()) {
            arrayList.add(add);
        }
        return arrayList;
    }

    public void set(Key key, Object obj) {
        this.a.put(key, obj);
    }
}
