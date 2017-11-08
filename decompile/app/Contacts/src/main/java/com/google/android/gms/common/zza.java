package com.google.android.gms.common;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* compiled from: Unknown */
public class zza implements ServiceConnection {
    private final BlockingQueue<IBinder> zzafA = new LinkedBlockingQueue();
    boolean zzafz = false;

    public void onServiceConnected(ComponentName name, IBinder service) {
        this.zzafA.add(service);
    }

    public void onServiceDisconnected(ComponentName name) {
    }

    public IBinder zzoJ() throws InterruptedException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("BlockingServiceConnection.getService() called on main thread");
        } else if (this.zzafz) {
            throw new IllegalStateException();
        } else {
            this.zzafz = true;
            return (IBinder) this.zzafA.take();
        }
    }
}
