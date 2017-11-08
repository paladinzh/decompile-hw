package com.fyusion.sdk.common.ext;

/* compiled from: Unknown */
public class b {
    private int a = 0;
    private boolean b = true;

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
