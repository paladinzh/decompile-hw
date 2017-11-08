package com.fyusion.sdk.core.util.pool;

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

    private T b(int i) {
        if (this.b >= this.c) {
            return null;
        }
        this.b++;
        return a(i);
    }

    protected abstract T a(int i);

    protected abstract boolean a(T t, int i);

    public T acquire(int i) {
        T acquire = this.a.acquire();
        if (acquire == null) {
            return b(i);
        }
        if (a(acquire, i)) {
            return acquire;
        }
        this.b--;
        return b(i);
    }

    public void clear() {
        while (this.a.acquire() != null) {
            this.b--;
        }
    }

    public T mustAcquire(int i) {
        T acquire = acquire(i);
        while (acquire == null) {
            try {
                synchronized (this.d) {
                    this.d.wait();
                    acquire = acquire(i);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return acquire;
    }

    public void release(T t) {
        this.a.release(t);
        synchronized (this.d) {
            this.d.notify();
        }
    }
}
