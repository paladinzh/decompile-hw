package com.google.android.gms.common.internal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
public final class zzk implements Callback {
    private final Handler mHandler;
    private final zza zzadN;
    private final ArrayList<ConnectionCallbacks> zzadO = new ArrayList();
    final ArrayList<ConnectionCallbacks> zzadP = new ArrayList();
    private final ArrayList<OnConnectionFailedListener> zzadQ = new ArrayList();
    private volatile boolean zzadR = false;
    private final AtomicInteger zzadS = new AtomicInteger(0);
    private boolean zzadT = false;
    private final Object zzpc = new Object();

    /* compiled from: Unknown */
    public interface zza {
        boolean isConnected();

        Bundle zzmw();
    }

    public zzk(Looper looper, zza zza) {
        this.zzadN = zza;
        this.mHandler = new Handler(looper, this);
    }

    public boolean handleMessage(Message msg) {
        if (msg.what != 1) {
            Log.wtf("GmsClientEvents", "Don't know how to handle this message.");
            return false;
        }
        ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) msg.obj;
        synchronized (this.zzpc) {
            if (this.zzadR && this.zzadN.isConnected() && this.zzadO.contains(connectionCallbacks)) {
                connectionCallbacks.onConnected(this.zzadN.zzmw());
            }
        }
        return true;
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        zzx.zzv(listener);
        synchronized (this.zzpc) {
            if (this.zzadO.contains(listener)) {
                Log.w("GmsClientEvents", "registerConnectionCallbacks(): listener " + listener + " is already registered");
            } else {
                this.zzadO.add(listener);
            }
        }
        if (this.zzadN.isConnected()) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, listener));
        }
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        zzx.zzv(listener);
        synchronized (this.zzpc) {
            if (this.zzadQ.contains(listener)) {
                Log.w("GmsClientEvents", "registerConnectionFailedListener(): listener " + listener + " is already registered");
            } else {
                this.zzadQ.add(listener);
            }
        }
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        zzx.zzv(listener);
        synchronized (this.zzpc) {
            if (!this.zzadO.remove(listener)) {
                Log.w("GmsClientEvents", "unregisterConnectionCallbacks(): listener " + listener + " not found");
            } else if (this.zzadT) {
                this.zzadP.add(listener);
            }
        }
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        zzx.zzv(listener);
        synchronized (this.zzpc) {
            if (!this.zzadQ.remove(listener)) {
                Log.w("GmsClientEvents", "unregisterConnectionFailedListener(): listener " + listener + " not found");
            }
        }
    }

    public void zzbB(int i) {
        boolean z = false;
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            z = true;
        }
        zzx.zza(z, "onUnintentionalDisconnection must only be called on the Handler thread");
        this.mHandler.removeMessages(1);
        synchronized (this.zzpc) {
            this.zzadT = true;
            ArrayList arrayList = new ArrayList(this.zzadO);
            int i2 = this.zzadS.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) it.next();
                if (!this.zzadR || this.zzadS.get() != i2) {
                    break;
                } else if (this.zzadO.contains(connectionCallbacks)) {
                    connectionCallbacks.onConnectionSuspended(i);
                }
            }
            this.zzadP.clear();
            this.zzadT = false;
        }
    }

    public void zzh(Bundle bundle) {
        boolean z = false;
        zzx.zza(Looper.myLooper() == this.mHandler.getLooper(), "onConnectionSuccess must only be called on the Handler thread");
        synchronized (this.zzpc) {
            zzx.zzY(!this.zzadT);
            this.mHandler.removeMessages(1);
            this.zzadT = true;
            if (this.zzadP.size() == 0) {
                z = true;
            }
            zzx.zzY(z);
            ArrayList arrayList = new ArrayList(this.zzadO);
            int i = this.zzadS.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) it.next();
                if (!this.zzadR || !this.zzadN.isConnected() || this.zzadS.get() != i) {
                    break;
                } else if (!this.zzadP.contains(connectionCallbacks)) {
                    connectionCallbacks.onConnected(bundle);
                }
            }
            this.zzadP.clear();
            this.zzadT = false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void zzj(ConnectionResult connectionResult) {
        boolean z = false;
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            z = true;
        }
        zzx.zza(z, "onConnectionFailure must only be called on the Handler thread");
        this.mHandler.removeMessages(1);
        synchronized (this.zzpc) {
            ArrayList arrayList = new ArrayList(this.zzadQ);
            int i = this.zzadS.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                OnConnectionFailedListener onConnectionFailedListener = (OnConnectionFailedListener) it.next();
                if (this.zzadR && this.zzadS.get() == i) {
                    if (this.zzadQ.contains(onConnectionFailedListener)) {
                        onConnectionFailedListener.onConnectionFailed(connectionResult);
                    }
                }
            }
        }
    }

    public void zzoK() {
        this.zzadR = false;
        this.zzadS.incrementAndGet();
    }

    public void zzoL() {
        this.zzadR = true;
    }
}
