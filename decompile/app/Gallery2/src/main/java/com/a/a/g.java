package com.a.a;

import android.annotation.TargetApi;
import android.net.TrafficStats;
import android.os.Build.VERSION;
import android.os.Process;
import android.os.SystemClock;
import java.util.concurrent.BlockingQueue;

/* compiled from: Unknown */
public class g extends Thread {
    private final BlockingQueue<l<?>> a;
    private final f b;
    private final b c;
    private final o d;
    private volatile boolean e = false;

    public g(BlockingQueue<l<?>> blockingQueue, f fVar, b bVar, o oVar) {
        this.a = blockingQueue;
        this.b = fVar;
        this.c = bVar;
        this.d = oVar;
    }

    @TargetApi(14)
    private void a(l<?> lVar) {
        if (VERSION.SDK_INT >= 14) {
            TrafficStats.setThreadStatsTag(lVar.b());
        }
    }

    private void a(l<?> lVar, s sVar) {
        this.d.a((l) lVar, lVar.a(sVar));
    }

    public void a() {
        this.e = true;
        interrupt();
    }

    public void run() {
        Process.setThreadPriority(10);
        while (true) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            try {
                l lVar = (l) this.a.take();
                try {
                    lVar.a("network-queue-take");
                    if (lVar.g()) {
                        lVar.b("network-discard-cancelled");
                    } else {
                        a(lVar);
                        i a = this.b.a(lVar);
                        lVar.a("network-http-complete");
                        if (a.d) {
                            if (lVar.v()) {
                                lVar.b("not-modified");
                            }
                        }
                        n a2 = lVar.a(a);
                        lVar.a("network-parse-complete");
                        if (lVar.q() && a2.b != null) {
                            this.c.a(lVar.e(), a2.b);
                            lVar.a("network-cache-written");
                        }
                        lVar.u();
                        this.d.a(lVar, a2);
                    }
                } catch (s e) {
                    e.a(SystemClock.elapsedRealtime() - elapsedRealtime);
                    a(lVar, e);
                } catch (Throwable e2) {
                    t.a(e2, "Unhandled exception %s", e2.toString());
                    s sVar = new s(e2);
                    sVar.a(SystemClock.elapsedRealtime() - elapsedRealtime);
                    this.d.a(lVar, sVar);
                }
            } catch (InterruptedException e3) {
                if (this.e) {
                    return;
                }
            }
        }
    }
}
