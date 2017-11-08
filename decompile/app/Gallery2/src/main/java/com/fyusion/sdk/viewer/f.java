package com.fyusion.sdk.viewer;

import android.support.v4.util.Pools$Pool;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.viewer.internal.b.c.g;
import com.fyusion.sdk.viewer.internal.b.c.h;
import com.fyusion.sdk.viewer.internal.b.c.i;
import java.util.List;

/* compiled from: Unknown */
public class f {
    private final i a = new i(this.b);
    private final Pools$Pool<List<Exception>> b = com.fyusion.sdk.viewer.internal.f.a.a.a();
    private e c;

    /* compiled from: Unknown */
    public static class a extends RuntimeException {
        public a(String str) {
            super(str);
        }
    }

    /* compiled from: Unknown */
    public static class b extends a {
        public b(Object obj) {
            super("Failed to find any ModelLoaders for model: " + obj);
        }
    }

    public e a() {
        return this.c;
    }

    public f a(e eVar) {
        this.c = eVar;
        return this;
    }

    public <Model, Data> f a(Class<Model> cls, Class<Data> cls2, h<Model, Data> hVar) {
        DLog.i("Registry", "Register module: " + cls.getSimpleName() + ":" + cls2.getSimpleName() + ":" + hVar + " " + this);
        this.a.a(cls, cls2, hVar);
        return this;
    }

    public <Model> List<g<Model, ?>> a(Model model) {
        List<g<Model, ?>> a = this.a.a((Object) model);
        if (!a.isEmpty()) {
            return a;
        }
        throw new b(model);
    }
}
