package com.google.android.gms.internal;

/* compiled from: Unknown */
public abstract class ct {
    private final Runnable kW = new Runnable(this) {
        final /* synthetic */ ct pJ;

        {
            this.pJ = r1;
        }

        public final void run() {
            this.pJ.pI = Thread.currentThread();
            this.pJ.aB();
        }
    };
    private volatile Thread pI;

    public abstract void aB();

    public final void start() {
        cu.execute(this.kW);
    }
}
