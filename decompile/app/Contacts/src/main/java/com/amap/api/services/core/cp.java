package com.amap.api.services.core;

/* compiled from: ThreadTask */
public abstract class cp implements Runnable {
    a d;

    /* compiled from: ThreadTask */
    interface a {
        void a(cp cpVar);

        void b(cp cpVar);
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
            av.a(th, "ThreadTask", "run");
            th.printStackTrace();
        }
    }
}
