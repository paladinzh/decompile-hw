package com.amap.api.mapcore.util;

/* compiled from: ThreadTask */
public abstract class dp implements Runnable {
    a d;

    /* compiled from: ThreadTask */
    interface a {
        void a(dp dpVar);

        void b(dp dpVar);

        void c(dp dpVar);
    }

    public abstract void a();

    public final void run() {
        try {
            if (this.d != null) {
                this.d.a(this);
            }
            if (!Thread.interrupted()) {
                a();
                if (!(Thread.interrupted() || this.d == null)) {
                    this.d.b(this);
                }
            }
        } catch (Throwable th) {
            ce.a(th, "ThreadTask", "run");
            th.printStackTrace();
        }
    }

    public final void e() {
        try {
            if (this.d != null) {
                this.d.c(this);
            }
        } catch (Throwable th) {
            ce.a(th, "ThreadTask", "cancelTask");
            th.printStackTrace();
        }
    }
}
