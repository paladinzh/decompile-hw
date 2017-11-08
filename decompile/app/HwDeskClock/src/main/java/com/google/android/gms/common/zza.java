package com.google.android.gms.common;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/* compiled from: Unknown */
public class zza implements ServiceConnection {
    boolean zzYg = false;
    private final BlockingQueue<IBinder> zzYh = new LinkedBlockingQueue();

    public void onServiceConnected(ComponentName name, IBinder service) {
        this.zzYh.add(service);
    }

    public void onServiceDisconnected(ComponentName name) {
    }
}
