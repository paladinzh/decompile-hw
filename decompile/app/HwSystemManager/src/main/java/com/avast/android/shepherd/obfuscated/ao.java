package com.avast.android.shepherd.obfuscated;

import android.util.SparseArray;
import java.util.EnumSet;
import java.util.Iterator;

/* compiled from: Unknown */
public enum ao {
    NOTHING(0);
    
    private static final SparseArray<ao> b = null;
    private final int c;

    static {
        b = new SparseArray();
        Iterator it = EnumSet.allOf(ao.class).iterator();
        while (it.hasNext()) {
            ao aoVar = (ao) it.next();
            b.put(aoVar.a(), aoVar);
        }
    }

    private ao(int i) {
        this.c = i;
    }

    public final int a() {
        return this.c;
    }
}
