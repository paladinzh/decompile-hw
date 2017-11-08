package com.amap.api.mapcore.util;

/* compiled from: ThreadTask */
public abstract class hm implements Runnable {
    a d;

    /* compiled from: ThreadTask */
    interface a {
        void a(hm hmVar);

        void b(hm hmVar);

        void c(hm hmVar);
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
            fo.b(th, "ThreadTask", "run");
            th.printStackTrace();
        }
    }

    public final void e() {
        try {
            if (this.d != null) {
                this.d.c(this);
            }
        } catch (Throwable th) {
            fo.b(th, "ThreadTask", "cancelTask");
            th.printStackTrace();
        }
    }
}
