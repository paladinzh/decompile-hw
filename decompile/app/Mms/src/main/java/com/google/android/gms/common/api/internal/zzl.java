package com.google.android.gms.common.api.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.zzc;
import com.google.android.gms.internal.zzrn;
import com.google.android.gms.internal.zzro;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/* compiled from: Unknown */
public class zzl implements zzp {
    private final Context mContext;
    private final Lock zzXG;
    final zzj zzagW;
    private final zzc zzags;
    final com.google.android.gms.common.api.Api.zza<? extends zzrn, zzro> zzagt;
    final Map<Api<?>, Integer> zzahA;
    final Map<Api.zzc<?>, com.google.android.gms.common.api.Api.zzb> zzahT;
    final zzf zzahz;
    private final Condition zzaim;
    private final zzb zzain;
    final Map<Api.zzc<?>, ConnectionResult> zzaio = new HashMap();
    private volatile zzk zzaip;
    private ConnectionResult zzaiq = null;
    int zzair;
    final com.google.android.gms.common.api.internal.zzp.zza zzais;

    /* compiled from: Unknown */
    static abstract class zza {
        private final zzk zzait;

        protected zza(zzk zzk) {
            this.zzait = zzk;
        }

        public final void zzd(zzl zzl) {
            zzl.zzXG.lock();
            try {
                if (zzl.zzaip == this.zzait) {
                    zzpt();
                } else {
                    zzl.zzXG.unlock();
                }
            } finally {
                zzl.zzXG.unlock();
            }
        }

        protected abstract void zzpt();
    }

    /* compiled from: Unknown */
    final class zzb extends Handler {
        final /* synthetic */ zzl zzaiu;

        zzb(zzl zzl, Looper looper) {
            this.zzaiu = zzl;
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ((zza) msg.obj).zzd(this.zzaiu);
                    return;
                case 2:
                    throw ((RuntimeException) msg.obj);
                default:
                    Log.w("GACStateManager", "Unknown message id: " + msg.what);
                    return;
            }
        }
    }

    public zzl(Context context, zzj zzj, Lock lock, Looper looper, zzc zzc, Map<Api.zzc<?>, com.google.android.gms.common.api.Api.zzb> map, zzf zzf, Map<Api<?>, Integer> map2, com.google.android.gms.common.api.Api.zza<? extends zzrn, zzro> zza, ArrayList<zzc> arrayList, com.google.android.gms.common.api.internal.zzp.zza zza2) {
        this.mContext = context;
        this.zzXG = lock;
        this.zzags = zzc;
        this.zzahT = map;
        this.zzahz = zzf;
        this.zzahA = map2;
        this.zzagt = zza;
        this.zzagW = zzj;
        this.zzais = zza2;
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            ((zzc) it.next()).zza(this);
        }
        this.zzain = new zzb(this, looper);
        this.zzaim = lock.newCondition();
        this.zzaip = new zzi(this);
    }

    public ConnectionResult blockingConnect() {
        connect();
        while (isConnecting()) {
            try {
                this.zzaim.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ConnectionResult(15, null);
            }
        }
        return !isConnected() ? this.zzaiq == null ? new ConnectionResult(13, null) : this.zzaiq : ConnectionResult.zzafB;
    }

    public ConnectionResult blockingConnect(long timeout, TimeUnit unit) {
        connect();
        long toNanos = unit.toNanos(timeout);
        while (isConnecting()) {
            if ((toNanos > 0 ? 1 : null) == null) {
                try {
                    disconnect();
                    return new ConnectionResult(14, null);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new ConnectionResult(15, null);
                }
            }
            toNanos = this.zzaim.awaitNanos(toNanos);
        }
        return !isConnected() ? this.zzaiq == null ? new ConnectionResult(13, null) : this.zzaiq : ConnectionResult.zzafB;
    }

    public void connect() {
        this.zzaip.connect();
    }

    public boolean disconnect() {
        boolean disconnect = this.zzaip.disconnect();
        if (disconnect) {
            this.zzaio.clear();
        }
        return disconnect;
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        String str = prefix + "  ";
        for (Api api : this.zzahA.keySet()) {
            writer.append(prefix).append(api.getName()).println(":");
            ((com.google.android.gms.common.api.Api.zzb) this.zzahT.get(api.zzoR())).dump(str, fd, writer, args);
        }
    }

    @Nullable
    public ConnectionResult getConnectionResult(@NonNull Api<?> api) {
        Api.zzc zzoR = api.zzoR();
        if (this.zzahT.containsKey(zzoR)) {
            if (((com.google.android.gms.common.api.Api.zzb) this.zzahT.get(zzoR)).isConnected()) {
                return ConnectionResult.zzafB;
            }
            if (this.zzaio.containsKey(zzoR)) {
                return (ConnectionResult) this.zzaio.get(zzoR);
            }
        }
        return null;
    }

    public boolean isConnected() {
        return this.zzaip instanceof zzg;
    }

    public boolean isConnecting() {
        return this.zzaip instanceof zzh;
    }

    public void onConnected(@Nullable Bundle connectionHint) {
        this.zzXG.lock();
        try {
            this.zzaip.onConnected(connectionHint);
        } finally {
            this.zzXG.unlock();
        }
    }

    public void onConnectionSuspended(int cause) {
        this.zzXG.lock();
        try {
            this.zzaip.onConnectionSuspended(cause);
        } finally {
            this.zzXG.unlock();
        }
    }

    public <A extends com.google.android.gms.common.api.Api.zzb, R extends Result, T extends com.google.android.gms.common.api.internal.zza.zza<R, A>> T zza(@NonNull T t) {
        return this.zzaip.zza(t);
    }

    public void zza(@NonNull ConnectionResult connectionResult, @NonNull Api<?> api, int i) {
        this.zzXG.lock();
        try {
            this.zzaip.zza(connectionResult, api, i);
        } finally {
            this.zzXG.unlock();
        }
    }

    void zza(zza zza) {
        this.zzain.sendMessage(this.zzain.obtainMessage(1, zza));
    }

    void zza(RuntimeException runtimeException) {
        this.zzain.sendMessage(this.zzain.obtainMessage(2, runtimeException));
    }

    public boolean zza(zzu zzu) {
        return false;
    }

    public <A extends com.google.android.gms.common.api.Api.zzb, T extends com.google.android.gms.common.api.internal.zza.zza<? extends Result, A>> T zzb(@NonNull T t) {
        return this.zzaip.zzb(t);
    }

    void zzh(ConnectionResult connectionResult) {
        this.zzXG.lock();
        try {
            this.zzaiq = connectionResult;
            this.zzaip = new zzi(this);
            this.zzaip.begin();
            this.zzaim.signalAll();
        } finally {
            this.zzXG.unlock();
        }
    }

    public void zzoW() {
    }

    void zzpK() {
        this.zzXG.lock();
        try {
            this.zzaip = new zzh(this, this.zzahz, this.zzahA, this.zzags, this.zzagt, this.zzXG, this.mContext);
            this.zzaip.begin();
            this.zzaim.signalAll();
        } finally {
            this.zzXG.unlock();
        }
    }

    void zzpL() {
        this.zzXG.lock();
        try {
            this.zzagW.zzpF();
            this.zzaip = new zzg(this);
            this.zzaip.begin();
            this.zzaim.signalAll();
        } finally {
            this.zzXG.unlock();
        }
    }

    void zzpM() {
        for (com.google.android.gms.common.api.Api.zzb disconnect : this.zzahT.values()) {
            disconnect.disconnect();
        }
    }

    public void zzpj() {
        if (isConnected()) {
            ((zzg) this.zzaip).zzps();
        }
    }
}
