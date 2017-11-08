package com.google.android.gms.internal;

import android.os.Process;

/* compiled from: Unknown */
class zznl implements Runnable {
    private final int mPriority;
    private final Runnable zzx;

    public zznl(Runnable runnable, int i) {
        this.zzx = runnable;
        this.mPriority = i;
    }

    public void run() {
        Process.setThreadPriority(this.mPriority);
        this.zzx.run();
    }
}
