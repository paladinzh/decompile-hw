package com.fyusion.sdk.core.util.a;

import android.support.v4.util.Pools$SynchronizedPool;

/* compiled from: Unknown */
public abstract class a<T> {
    private Pools$SynchronizedPool<T> a;
    private int b;
    private int c;
    private final Object d = new Object();

    public a(int i) {
        this.c = i;
        this.a = new Pools$SynchronizedPool(i);
    }

    private T d(int i) {
        if (this.b >= this.c) {
            return null;
        }
        this.b++;
        return a(i);
    }

    protected abstract T a(int i);

    public void a() {
        while (this.a.acquire() != null) {
            this.b--;
        }
    }

    public void a(T t) {
        this.a.release(t);
        synchronized (this.d) {
            this.d.notify();
        }
    }

    protected abstract boolean a(T t, int i);

    public T b(int i) {
        T c = c(i);
        while (c == null) {
            try {
                synchronized (this.d) {
                    this.d.wait();
                    c = c(i);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return c;
    }

    public T c(int i) {
        T acquire = this.a.acquire();
        if (acquire == null) {
            return d(i);
        }
        if (a(acquire, i)) {
            return acquire;
        }
        this.b--;
        return d(i);
    }
}
