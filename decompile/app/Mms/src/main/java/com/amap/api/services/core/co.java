package com.amap.api.services.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* compiled from: ThreadPool */
public final class co {
    private static co a = null;
    private ExecutorService b;
    private ConcurrentHashMap<cp, Future<?>> c = new ConcurrentHashMap();
    private a d = new cq(this);

    public static synchronized co a(int i) {
        co coVar;
        synchronized (co.class) {
            if (a == null) {
                a = new co(i);
            }
            coVar = a;
        }
        return coVar;
    }

    private co(int i) {
        try {
            this.b = Executors.newFixedThreadPool(i);
        } catch (Throwable th) {
            av.a(th, "TPool", "ThreadPool");
            th.printStackTrace();
        }
    }

    private synchronized void a(cp cpVar, boolean z) {
        try {
            Future future = (Future) this.c.remove(cpVar);
            if (z && future != null) {
                future.cancel(true);
            }
        } catch (Throwable th) {
            av.a(th, "TPool", "removeQueue");
            th.printStackTrace();
        }
    }
}
