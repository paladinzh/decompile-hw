package com.fyusion.sdk.viewer.internal.f.a;

import android.support.v4.util.Pools$Pool;
import android.support.v4.util.Pools$SimplePool;
import android.support.v4.util.Pools$SynchronizedPool;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class a {
    private static final d<Object> a = new d<Object>() {
        public void a(Object obj) {
        }
    };

    /* compiled from: Unknown */
    public interface a<T> {
        T b();
    }

    /* compiled from: Unknown */
    public interface c {
        b k();
    }

    /* compiled from: Unknown */
    public interface d<T> {
        void a(T t);
    }

    /* compiled from: Unknown */
    private static final class b<T> implements Pools$Pool<T> {
        private final a<T> a;
        private final d<T> b;
        private final Pools$Pool<T> c;

        b(Pools$Pool<T> pools$Pool, a<T> aVar, d<T> dVar) {
            this.c = pools$Pool;
            this.a = aVar;
            this.b = dVar;
        }

        public T acquire() {
            T acquire = this.c.acquire();
            if (acquire == null) {
                acquire = this.a.b();
                if (Log.isLoggable("FactoryPools", 2)) {
                    Log.v("FactoryPools", "Created new " + acquire.getClass());
                }
            }
            if (acquire instanceof c) {
                ((c) acquire).k().a(false);
            }
            return acquire;
        }

        public boolean release(T t) {
            if (t instanceof c) {
                ((c) t).k().a(true);
            }
            this.b.a(t);
            return this.c.release(t);
        }
    }

    public static <T> Pools$Pool<List<T>> a() {
        return a(20);
    }

    public static <T> Pools$Pool<List<T>> a(int i) {
        return a(new Pools$SynchronizedPool(i), new a<List<T>>() {
            public List<T> a() {
                return new ArrayList();
            }

            public /* synthetic */ Object b() {
                return a();
            }
        }, new d<List<T>>() {
            public void a(List<T> list) {
                list.clear();
            }
        });
    }

    public static <T extends c> Pools$Pool<T> a(int i, a<T> aVar) {
        return a(new Pools$SimplePool(i), (a) aVar);
    }

    private static <T extends c> Pools$Pool<T> a(Pools$Pool<T> pools$Pool, a<T> aVar) {
        return a(pools$Pool, aVar, b());
    }

    private static <T> Pools$Pool<T> a(Pools$Pool<T> pools$Pool, a<T> aVar, d<T> dVar) {
        return new b(pools$Pool, aVar, dVar);
    }

    public static <T extends c> Pools$Pool<T> b(int i, a<T> aVar) {
        return a(new Pools$SynchronizedPool(i), (a) aVar);
    }

    private static <T> d<T> b() {
        return a;
    }
}
