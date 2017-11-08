package com.huawei.openalliance.ad.utils.b;

import android.util.SparseArray;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public enum f {
    ALL(0),
    VERBOSE(2),
    DEBUG(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    ASSERT(7),
    OUT(100),
    NONE(255);
    
    private static final SparseArray<f> j = null;
    private static final Map<String, f> k = null;
    private final int l;

    static {
        j = new SparseArray();
        k = new HashMap();
        f[] values = values();
        int length = values.length;
        int i;
        while (i < length) {
            Enum enumR = values[i];
            j.put(enumR.a(), enumR);
            k.put(enumR.name(), enumR);
            i++;
        }
    }

    private f(int i) {
        this.l = i;
    }

    public int a() {
        return this.l;
    }
}
