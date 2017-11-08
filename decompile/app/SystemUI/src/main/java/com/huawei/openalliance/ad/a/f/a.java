package com.huawei.openalliance.ad.a.f;

import android.content.Context;
import java.util.HashSet;
import java.util.Set;

/* compiled from: Unknown */
public abstract class a {
    private static Set<String> a = new HashSet();

    static {
        a.add("click");
        a.add("imp");
    }

    public abstract void a(Context context);

    public boolean a(String str) {
        return a.contains(str);
    }
}
