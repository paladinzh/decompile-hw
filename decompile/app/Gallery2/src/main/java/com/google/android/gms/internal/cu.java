package com.google.android.gms.internal;

import android.os.Process;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
public final class cu {
    private static final ThreadFactory pK = new ThreadFactory() {
        private final AtomicInteger pN = new AtomicInteger(1);

        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "AdWorker #" + this.pN.getAndIncrement());
        }
    };
    private static final ThreadPoolExecutor pL = new ThreadPoolExecutor(0, 10, 65, TimeUnit.SECONDS, new SynchronousQueue(true), pK);

    public static void execute(final Runnable task) {
        try {
            pL.execute(new Runnable() {
                public void run() {
                    Process.setThreadPriority(10);
                    task.run();
                }
            });
        } catch (Throwable e) {
            da.b("Too many background threads already running. Aborting task.", e);
        }
    }
}
