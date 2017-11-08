package com.fyusion.sdk.viewer.internal.b.c;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public interface e {
    @Deprecated
    public static final e a = new e() {
        public Map<String, String> a() {
            return Collections.emptyMap();
        }
    };
    public static final e b = new e() {
        public Map<String, String> a() {
            return new HashMap();
        }
    };

    Map<String, String> a();
}
