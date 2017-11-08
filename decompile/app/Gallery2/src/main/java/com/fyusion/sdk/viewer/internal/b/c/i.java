package com.fyusion.sdk.viewer.internal.b.c;

import android.support.v4.util.Pools$Pool;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class i {
    private final j a;
    private final a b = new a();

    /* compiled from: Unknown */
    private static class a {
        private final Map<Class<?>, a<?>> a;

        /* compiled from: Unknown */
        private static class a<Model> {
            private final List<g<Model, ?>> a;

            public a(List<g<Model, ?>> list) {
                this.a = list;
            }
        }

        private a() {
            this.a = new HashMap();
        }

        public <Model> List<g<Model, ?>> a(Class<Model> cls) {
            a aVar = (a) this.a.get(cls);
            return aVar != null ? aVar.a : null;
        }

        public <Model> void a(Class<Model> cls, List<g<Model, ?>> list) {
            if (((a) this.a.put(cls, new a(list))) != null) {
                throw new IllegalStateException("Already cached loaders for model: " + cls);
            }
        }
    }

    public i(Pools$Pool<List<Exception>> pools$Pool) {
        this.a = new j(pools$Pool);
    }

    private <A> List<g<A, ?>> a(Class<A> cls) {
        List<g<A, ?>> a = this.b.a(cls);
        if (a != null) {
            return a;
        }
        a = Collections.unmodifiableList(this.a.a((Class) cls));
        this.b.a(cls, a);
        return a;
    }

    private static <A> Class<A> b(A a) {
        return a.getClass();
    }

    public synchronized <A> List<g<A, ?>> a(A a) {
        List<g<A, ?>> arrayList;
        List a2 = a(b(a));
        int size = a2.size();
        arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            g gVar = (g) a2.get(i);
            if (gVar.a(a)) {
                arrayList.add(gVar);
            }
        }
        return arrayList;
    }

    public synchronized <Model, Data> void a(Class<Model> cls, Class<Data> cls2, h<Model, Data> hVar) {
        this.a.a(cls, cls2, hVar);
    }
}
