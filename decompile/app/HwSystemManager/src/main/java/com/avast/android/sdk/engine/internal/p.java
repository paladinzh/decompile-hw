package com.avast.android.sdk.engine.internal;

import com.avast.android.sdk.engine.VpsClassLoaderFactory;
import dalvik.system.DexClassLoader;

/* compiled from: Unknown */
public class p implements VpsClassLoaderFactory {
    public ClassLoader createVpsClassLoader(String str, String str2, ClassLoader classLoader) {
        return new DexClassLoader(str, str2, null, classLoader);
    }
}
