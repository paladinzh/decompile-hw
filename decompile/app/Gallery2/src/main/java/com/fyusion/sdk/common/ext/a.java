package com.fyusion.sdk.common.ext;

/* compiled from: Unknown */
public class a {
    private int a = 0;
    private boolean b = true;

    public void a(boolean z) {
        synchronized (this) {
            if (this.a != 0) {
                try {
                    wait();
                    if (z) {
                        this.b = false;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }

    public synchronized boolean a() {
        if (this.b) {
            this.a++;
            if (this.a == 0) {
                notifyAll();
            }
        }
        return this.b;
    }

    public synchronized boolean b() {
        if (this.b) {
            this.a--;
            if (this.a == 0) {
                notifyAll();
            }
        }
        return this.b;
    }
}
