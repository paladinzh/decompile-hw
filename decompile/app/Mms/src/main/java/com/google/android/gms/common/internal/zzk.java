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
    private final zza zzalQ;
    private final ArrayList<ConnectionCallbacks> zzalR = new ArrayList();
    final ArrayList<ConnectionCallbacks> zzalS = new ArrayList();
    private final ArrayList<OnConnectionFailedListener> zzalT = new ArrayList();
    private volatile boolean zzalU = false;
    private final AtomicInteger zzalV = new AtomicInteger(0);
    private boolean zzalW = false;
    private final Object zzpV = new Object();

    /* compiled from: Unknown */
    public interface zza {
        boolean isConnected();

        Bundle zzoi();
    }

    public zzk(Looper looper, zza zza) {
        this.zzalQ = zza;
        this.mHandler = new Handler(looper, this);
    }

    public boolean handleMessage(Message msg) {
        if (msg.what != 1) {
            Log.wtf("GmsClientEvents", "Don't know how to handle message: " + msg.what, new Exception());
            return false;
        }
        ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) msg.obj;
        synchronized (this.zzpV) {
            if (this.zzalU && this.zzalQ.isConnected() && this.zzalR.contains(connectionCallbacks)) {
                connectionCallbacks.onConnected(this.zzalQ.zzoi());
            }
        }
        return true;
    }

    public boolean isConnectionCallbacksRegistered(ConnectionCallbacks listener) {
        boolean contains;
        zzx.zzz(listener);
        synchronized (this.zzpV) {
            contains = this.zzalR.contains(listener);
        }
        return contains;
    }

    public boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener listener) {
        boolean contains;
        zzx.zzz(listener);
        synchronized (this.zzpV) {
            contains = this.zzalT.contains(listener);
        }
        return contains;
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        zzx.zzz(listener);
        synchronized (this.zzpV) {
            if (this.zzalR.contains(listener)) {
                Log.w("GmsClientEvents", "registerConnectionCallbacks(): listener " + listener + " is already registered");
            } else {
                this.zzalR.add(listener);
            }
        }
        if (this.zzalQ.isConnected()) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, listener));
        }
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        zzx.zzz(listener);
        synchronized (this.zzpV) {
            if (this.zzalT.contains(listener)) {
                Log.w("GmsClientEvents", "registerConnectionFailedListener(): listener " + listener + " is already registered");
            } else {
                this.zzalT.add(listener);
            }
        }
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        zzx.zzz(listener);
        synchronized (this.zzpV) {
            if (!this.zzalR.remove(listener)) {
                Log.w("GmsClientEvents", "unregisterConnectionCallbacks(): listener " + listener + " not found");
            } else if (this.zzalW) {
                this.zzalS.add(listener);
            }
        }
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        zzx.zzz(listener);
        synchronized (this.zzpV) {
            if (!this.zzalT.remove(listener)) {
                Log.w("GmsClientEvents", "unregisterConnectionFailedListener(): listener " + listener + " not found");
            }
        }
    }

    public void zzbT(int i) {
        boolean z = false;
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            z = true;
        }
        zzx.zza(z, (Object) "onUnintentionalDisconnection must only be called on the Handler thread");
        this.mHandler.removeMessages(1);
        synchronized (this.zzpV) {
            this.zzalW = true;
            ArrayList arrayList = new ArrayList(this.zzalR);
            int i2 = this.zzalV.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) it.next();
                if (!this.zzalU || this.zzalV.get() != i2) {
                    break;
                } else if (this.zzalR.contains(connectionCallbacks)) {
                    connectionCallbacks.onConnectionSuspended(i);
                }
            }
            this.zzalS.clear();
            this.zzalW = false;
        }
    }

    public void zzk(Bundle bundle) {
        boolean z = false;
        zzx.zza(Looper.myLooper() == this.mHandler.getLooper(), (Object) "onConnectionSuccess must only be called on the Handler thread");
        synchronized (this.zzpV) {
            zzx.zzab(!this.zzalW);
            this.mHandler.removeMessages(1);
            this.zzalW = true;
            if (this.zzalS.size() == 0) {
                z = true;
            }
            zzx.zzab(z);
            ArrayList arrayList = new ArrayList(this.zzalR);
            int i = this.zzalV.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) it.next();
                if (!this.zzalU || !this.zzalQ.isConnected() || this.zzalV.get() != i) {
                    break;
                } else if (!this.zzalS.contains(connectionCallbacks)) {
                    connectionCallbacks.onConnected(bundle);
                }
            }
            this.zzalS.clear();
            this.zzalW = false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void zzk(ConnectionResult connectionResult) {
        boolean z = false;
        if (Looper.myLooper() == this.mHandler.getLooper()) {
            z = true;
        }
        zzx.zza(z, (Object) "onConnectionFailure must only be called on the Handler thread");
        this.mHandler.removeMessages(1);
        synchronized (this.zzpV) {
            ArrayList arrayList = new ArrayList(this.zzalT);
            int i = this.zzalV.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                OnConnectionFailedListener onConnectionFailedListener = (OnConnectionFailedListener) it.next();
                if (this.zzalU && this.zzalV.get() == i) {
                    if (this.zzalT.contains(onConnectionFailedListener)) {
                        onConnectionFailedListener.onConnectionFailed(connectionResult);
                    }
                }
            }
        }
    }

    public void zzqQ() {
        this.zzalU = false;
        this.zzalV.incrementAndGet();
    }

    public void zzqR() {
        this.zzalU = true;
    }
}
