package com.amap.api.services.core;

/* compiled from: ThreadTask */
public abstract class ax implements Runnable {
    a a;

    /* compiled from: ThreadTask */
    interface a {
        void a(ax axVar);

        void b(ax axVar);
    }

    public abstract void a();

    public final void run() {
        try {
            if (this.a != null) {
                this.a.a(this);
            }
            if (!Thread.interrupted()) {
                a();
                if (!(Thread.interrupted() || this.a == null)) {
                    this.a.b(this);
                }
            }
        } catch (Throwable th) {
            ay.a(th, "ThreadTask", "run");
            th.printStackTrace();
        }
    }
}
