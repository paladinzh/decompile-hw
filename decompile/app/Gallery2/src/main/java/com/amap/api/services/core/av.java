package com.amap.api.services.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/* compiled from: ThreadPool */
public final class av {
    private static av a = null;
    private ExecutorService b;
    private ConcurrentHashMap<ax, Future<?>> c = new ConcurrentHashMap();
    private a d = new aw(this);

    public static synchronized av a(int i) {
        av avVar;
        synchronized (av.class) {
            if (a == null) {
                a = new av(i);
            }
            avVar = a;
        }
        return avVar;
    }

    private av(int i) {
        try {
            this.b = Executors.newFixedThreadPool(i);
        } catch (Throwable th) {
            ay.a(th, "TPool", "ThreadPool");
            th.printStackTrace();
        }
    }

    private synchronized void a(ax axVar, boolean z) {
        try {
            Future future = (Future) this.c.remove(axVar);
            if (z && future != null) {
                future.cancel(true);
            }
        } catch (Throwable th) {
            ay.a(th, "TPool", "removeQueue");
            th.printStackTrace();
        }
    }
}
