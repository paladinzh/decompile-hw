package com.fyusion.sdk.common.ext.a;

import com.fyusion.sdk.common.ext.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class b {
    private Map<Key, Object> a = new HashMap();

    public <T> T a(Key<T> key) {
        T t = this.a.get(key);
        return t == null ? null : t;
    }

    public List<Key> a() {
        List<Key> arrayList = new ArrayList();
        for (Key add : this.a.keySet()) {
            arrayList.add(add);
        }
        return arrayList;
    }
}
