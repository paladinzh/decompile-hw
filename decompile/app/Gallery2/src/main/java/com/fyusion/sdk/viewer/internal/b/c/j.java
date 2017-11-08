package com.fyusion.sdk.viewer.internal.b.c;

import android.support.annotation.Nullable;
import android.support.v4.util.Pools$Pool;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.viewer.internal.f.d;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public class j {
    private static final g<Object, Object> a = new a();
    private List<b<?, ?>> b = new ArrayList();
    private final Set<b<?, ?>> c = new HashSet();
    private final Pools$Pool<List<Exception>> d;

    /* compiled from: Unknown */
    private static class a implements g<Object, Object> {
        private a() {
        }

        @Nullable
        public com.fyusion.sdk.viewer.internal.b.c.g.a<Object> a(Object obj, boolean z) {
            return null;
        }

        public boolean a(Object obj) {
            return false;
        }
    }

    /* compiled from: Unknown */
    private static class b<Model, Data> {
        private final Class<Model> a;
        private final Class<Data> b;
        private final h<Model, Data> c;

        public b(Class<Model> cls, Class<Data> cls2, h<Model, Data> hVar) {
            this.a = cls;
            this.b = cls2;
            this.c = hVar;
        }

        public boolean a(Class<?> cls) {
            return this.a.isAssignableFrom(cls);
        }

        public boolean equals(Object obj) {
            b bVar = (b) obj;
            if (!super.equals(obj)) {
                if (this.a.equals(bVar.a) && this.b.equals(bVar.b)) {
                    if (!this.c.getClass().equals(this.c.getClass())) {
                    }
                }
                return false;
            }
            return true;
        }

        public String toString() {
            return this.a.getSimpleName() + ":" + this.b.getSimpleName() + ":" + this.c;
        }
    }

    public j(Pools$Pool<List<Exception>> pools$Pool) {
        this.d = pools$Pool;
    }

    private <Model, Data> g<Model, Data> a(b<?, ?> bVar) {
        return (g) d.a(bVar.c.a(this));
    }

    private <Model, Data> void a(Class<Model> cls, Class<Data> cls2, h<Model, Data> hVar, boolean z) {
        int i = 0;
        b bVar = new b(cls, cls2, hVar);
        if (this.b.contains(bVar)) {
            DLog.d("MultiModelLoaderFactory", "Model Loader has been registered: " + bVar);
            return;
        }
        List list = this.b;
        if (z) {
            i = this.b.size();
        }
        list.add(i, bVar);
    }

    synchronized <Model> List<g<Model, ?>> a(Class<Model> cls) {
        List<g<Model, ?>> arrayList;
        try {
            arrayList = new ArrayList();
            for (b bVar : this.b) {
                if (!this.c.contains(bVar) && bVar.a((Class) cls)) {
                    this.c.add(bVar);
                    arrayList.add(a(bVar));
                    this.c.remove(bVar);
                }
            }
        } catch (Throwable th) {
            this.c.clear();
        }
        return arrayList;
    }

    synchronized <Model, Data> void a(Class<Model> cls, Class<Data> cls2, h<Model, Data> hVar) {
        a(cls, cls2, hVar, true);
    }
}
