package com.amap.api.mapcore.util;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/* compiled from: ThreadPool */
public final class dn {
    private static dn a = null;
    private ExecutorService b;
    private ConcurrentHashMap<dp, Future<?>> c = new ConcurrentHashMap();
    private a d = new do(this);

    public static synchronized dn a(int i) {
        dn dnVar;
        synchronized (dn.class) {
            if (a == null) {
                a = new dn(i);
            }
            dnVar = a;
        }
        return dnVar;
    }

    private dn(int i) {
        try {
            this.b = Executors.newFixedThreadPool(i);
        } catch (Throwable th) {
            ce.a(th, "TPool", "ThreadPool");
            th.printStackTrace();
        }
    }

    public void a(dp dpVar) throws bk {
        try {
            if (!b(dpVar) && this.b != null && !this.b.isShutdown()) {
                dpVar.d = this.d;
                try {
                    Future submit = this.b.submit(dpVar);
                    if (submit != null) {
                        a(dpVar, submit);
                    }
                } catch (RejectedExecutionException e) {
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
            ce.a(th, "TPool", "addTask");
            bk bkVar = new bk("thread pool has exception");
        }
    }

    public static synchronized void a() {
        synchronized (dn.class) {
            try {
                if (a != null) {
                    a.b();
                    a = null;
                }
            } catch (Throwable th) {
                ce.a(th, "TPool", "onDestroy");
                th.printStackTrace();
            }
        }
    }

    private void b() {
        try {
            for (Entry key : this.c.entrySet()) {
                Future future = (Future) this.c.get((dp) key.getKey());
                if (future != null) {
                    future.cancel(true);
                }
            }
            this.c.clear();
            this.b.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            ce.a(th, "TPool", "destroy");
            th.printStackTrace();
        }
    }

    private synchronized boolean b(dp dpVar) {
        boolean containsKey;
        try {
            containsKey = this.c.containsKey(dpVar);
        } catch (Throwable th) {
            ce.a(th, "TPool", "contain");
            th.printStackTrace();
            containsKey = false;
        }
        return containsKey;
    }

    private synchronized void a(dp dpVar, Future<?> future) {
        try {
            this.c.put(dpVar, future);
        } catch (Throwable th) {
            ce.a(th, "TPool", "addQueue");
            th.printStackTrace();
        }
    }

    private synchronized void a(dp dpVar, boolean z) {
        try {
            Future future = (Future) this.c.remove(dpVar);
            if (z && future != null) {
                future.cancel(true);
            }
        } catch (Throwable th) {
            ce.a(th, "TPool", "removeQueue");
            th.printStackTrace();
        }
    }
}
