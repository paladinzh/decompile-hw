package com.amap.api.mapcore.util;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: ThreadPool */
public final class hl {
    private static hl a = null;
    private ExecutorService b;
    private ConcurrentHashMap<hm, Future<?>> c = new ConcurrentHashMap();
    private a d = new a(this) {
        final /* synthetic */ hl a;

        {
            this.a = r1;
        }

        public void a(hm hmVar) {
        }

        public void b(hm hmVar) {
            this.a.a(hmVar, false);
        }

        public void c(hm hmVar) {
            this.a.a(hmVar, true);
        }
    };

    public static synchronized hl a(int i) {
        hl hlVar;
        synchronized (hl.class) {
            if (a == null) {
                a = new hl(i);
            }
            hlVar = a;
        }
        return hlVar;
    }

    private hl(int i) {
        try {
            this.b = Executors.newFixedThreadPool(i);
        } catch (Throwable th) {
            fo.b(th, "TPool", "ThreadPool");
            th.printStackTrace();
        }
    }

    public void a(hm hmVar) throws ex {
        try {
            if (!b(hmVar) && this.b != null && !this.b.isShutdown()) {
                hmVar.d = this.d;
                try {
                    Future submit = this.b.submit(hmVar);
                    if (submit != null) {
                        a(hmVar, submit);
                    }
                } catch (RejectedExecutionException e) {
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
            fo.b(th, "TPool", "addTask");
            ex exVar = new ex("thread pool has exception");
        }
    }

    public static synchronized void a() {
        synchronized (hl.class) {
            try {
                if (a != null) {
                    a.b();
                    a = null;
                }
            } catch (Throwable th) {
                fo.b(th, "TPool", "onDestroy");
                th.printStackTrace();
            }
        }
    }

    private void b() {
        try {
            for (Entry key : this.c.entrySet()) {
                Future future = (Future) this.c.get((hm) key.getKey());
                if (future != null) {
                    future.cancel(true);
                }
            }
            this.c.clear();
            this.b.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            fo.b(th, "TPool", "destroy");
            th.printStackTrace();
        }
    }

    private synchronized boolean b(hm hmVar) {
        boolean containsKey;
        try {
            containsKey = this.c.containsKey(hmVar);
        } catch (Throwable th) {
            fo.b(th, "TPool", "contain");
            th.printStackTrace();
            containsKey = false;
        }
        return containsKey;
    }

    private synchronized void a(hm hmVar, Future<?> future) {
        try {
            this.c.put(hmVar, future);
        } catch (Throwable th) {
            fo.b(th, "TPool", "addQueue");
            th.printStackTrace();
        }
    }

    private synchronized void a(hm hmVar, boolean z) {
        try {
            Future future = (Future) this.c.remove(hmVar);
            if (z && future != null) {
                future.cancel(true);
            }
        } catch (Throwable th) {
            fo.b(th, "TPool", "removeQueue");
            th.printStackTrace();
        }
    }
}
