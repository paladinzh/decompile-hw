package com.amap.api.mapcore.util;

import android.content.Context;
import dalvik.system.DexFile;
import java.util.HashMap;
import java.util.Map;

/* compiled from: BaseClassLoader */
abstract class gk extends ClassLoader {
    protected final Context a;
    protected final Map<String, Class<?>> b = new HashMap();
    protected DexFile c = null;
    volatile boolean d = true;
    protected fh e;
    protected String f;

    public gk(Context context, fh fhVar, boolean z) {
        super(context.getClassLoader());
        this.a = context;
        this.e = fhVar;
    }

    public boolean a() {
        return this.c != null;
    }

    protected void b() {
        try {
            synchronized (this.b) {
                this.b.clear();
            }
            if (this.c != null) {
                this.c.close();
            }
        } catch (Throwable th) {
            gs.a(th, "BaseClassLoader", "releaseDexFile()");
        }
    }
}
