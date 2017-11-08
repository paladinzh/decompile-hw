package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.d$a;
import java.util.Map;
import java.util.Set;

/* compiled from: Unknown */
abstract class aj {
    private final Set<String> UW;

    boolean a(Set<String> set) {
        return set.containsAll(this.UW);
    }

    public abstract boolean iy();

    public Set<String> jd() {
        return this.UW;
    }

    public abstract d$a u(Map<String, d$a> map);
}
