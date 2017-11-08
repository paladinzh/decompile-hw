package com.google.android.gms.common.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Api.ApiOptions;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzac;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzk;
import com.google.android.gms.common.internal.zzx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* compiled from: Unknown */
final class zzi implements GoogleApiClient {
    private final Context mContext;
    private final Looper zzYV;
    final zzf zzZH;
    final Map<Api<?>, Integer> zzZI;
    private final Condition zzZY;
    final zzk zzZZ;
    private final int zzZf;
    private final int zzZg;
    private final GoogleApiAvailability zzZi;
    final com.google.android.gms.common.api.Api.zza<? extends com.google.android.gms.signin.zzd, com.google.android.gms.signin.zze> zzZj;
    private final Lock zzZs = new ReentrantLock();
    final Queue<zze<?>> zzaaa = new LinkedList();
    private volatile boolean zzaab;
    private long zzaac = 120000;
    private long zzaad = 5000;
    private final zza zzaae;
    BroadcastReceiver zzaaf;
    final Map<com.google.android.gms.common.api.Api.zzc<?>, com.google.android.gms.common.api.Api.zzb> zzaag = new HashMap();
    final Map<com.google.android.gms.common.api.Api.zzc<?>, ConnectionResult> zzaah = new HashMap();
    Set<Scope> zzaai = new HashSet();
    private volatile zzj zzaaj;
    private ConnectionResult zzaak = null;
    private final Set<zzl<?>> zzaal = Collections.newSetFromMap(new WeakHashMap());
    final Set<zze<?>> zzaam = Collections.newSetFromMap(new ConcurrentHashMap(16, 0.75f, 2));
    private final zzd zzaan = new zzd(this) {
        final /* synthetic */ zzi zzaaq;

        {
            this.zzaaq = r1;
        }

        public void zzc(zze<?> zze) {
            this.zzaaq.zzaam.remove(zze);
        }
    };
    private final ConnectionCallbacks zzaao = new ConnectionCallbacks(this) {
        final /* synthetic */ zzi zzaaq;

        {
            this.zzaaq = r1;
        }

        public void onConnected(Bundle connectionHint) {
            this.zzaaq.zzZs.lock();
            try {
                this.zzaaq.zzaaj.onConnected(connectionHint);
            } finally {
                this.zzaaq.zzZs.unlock();
            }
        }

        public void onConnectionSuspended(int cause) {
            this.zzaaq.zzZs.lock();
            try {
                this.zzaaq.zzaaj.onConnectionSuspended(cause);
            } finally {
                this.zzaaq.zzZs.unlock();
            }
        }
    };
    private final com.google.android.gms.common.internal.zzk.zza zzaap = new com.google.android.gms.common.internal.zzk.zza(this) {
        final /* synthetic */ zzi zzaaq;

        {
            this.zzaaq = r1;
        }

        public boolean isConnected() {
            return this.zzaaq.isConnected();
        }

        public Bundle zzmw() {
            return null;
        }
    };

    /* compiled from: Unknown */
    interface zze<A extends com.google.android.gms.common.api.Api.zzb> {
        void cancel();

        void zza(zzd zzd);

        void zzb(A a) throws DeadObjectException;

        com.google.android.gms.common.api.Api.zzc<A> zznd();

        int zznh();

        void zzw(Status status);

        void zzx(Status status);
    }

    /* compiled from: Unknown */
    static abstract class zzb {
        private final zzj zzaax;

        protected zzb(zzj zzj) {
            this.zzaax = zzj;
        }

        public final void zzf(zzi zzi) {
            zzi.zzZs.lock();
            try {
                if (zzi.zzaaj == this.zzaax) {
                    zzno();
                } else {
                    zzi.zzZs.unlock();
                }
            } finally {
                zzi.zzZs.unlock();
            }
        }

        protected abstract void zzno();
    }

    /* compiled from: Unknown */
    interface zzd {
        void zzc(zze<?> zze);
    }

    /* compiled from: Unknown */
    final class zza extends Handler {
        final /* synthetic */ zzi zzaaq;

        zza(zzi zzi, Looper looper) {
            this.zzaaq = zzi;
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.zzaaq.zznD();
                    return;
                case 2:
                    this.zzaaq.resume();
                    return;
                case 3:
                    ((zzb) msg.obj).zzf(this.zzaaq);
                    return;
                case MetaballPath.POINT_NUM /*4*/:
                    throw ((RuntimeException) msg.obj);
                default:
                    Log.w("GoogleApiClientImpl", "Unknown message id: " + msg.what);
                    return;
            }
        }
    }

    /* compiled from: Unknown */
    private static class zzc extends BroadcastReceiver {
        private WeakReference<zzi> zzaay;

        zzc(zzi zzi) {
            this.zzaay = new WeakReference(zzi);
        }

        public void onReceive(Context context, Intent intent) {
            String str = null;
            Uri data = intent.getData();
            if (data != null) {
                str = data.getSchemeSpecificPart();
            }
            if (str != null && str.equals("com.google.android.gms")) {
                zzi zzi = (zzi) this.zzaay.get();
                if (zzi != null) {
                    zzi.resume();
                }
            }
        }
    }

    public zzi(Context context, Looper looper, zzf zzf, GoogleApiAvailability googleApiAvailability, com.google.android.gms.common.api.Api.zza<? extends com.google.android.gms.signin.zzd, com.google.android.gms.signin.zze> zza, Map<Api<?>, ApiOptions> map, ArrayList<ConnectionCallbacks> arrayList, ArrayList<OnConnectionFailedListener> arrayList2, int i, int i2) {
        this.mContext = context;
        this.zzZZ = new zzk(looper, this.zzaap);
        this.zzYV = looper;
        this.zzaae = new zza(this, looper);
        this.zzZi = googleApiAvailability;
        this.zzZf = i;
        this.zzZg = i2;
        this.zzZI = new HashMap();
        this.zzZY = this.zzZs.newCondition();
        this.zzaaj = new zzh(this);
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            this.zzZZ.registerConnectionCallbacks((ConnectionCallbacks) it.next());
        }
        it = arrayList2.iterator();
        while (it.hasNext()) {
            this.zzZZ.registerConnectionFailedListener((OnConnectionFailedListener) it.next());
        }
        Map zzol = zzf.zzol();
        for (Api api : map.keySet()) {
            Object obj = map.get(api);
            int i3 = 0;
            if (zzol.get(api) != null) {
                i3 = !((com.google.android.gms.common.internal.zzf.zza) zzol.get(api)).zzadh ? 2 : 1;
            }
            int i4 = i3;
            this.zzZI.put(api, Integer.valueOf(i4));
            this.zzaag.put(api.zznd(), !api.zzne() ? zza(api.zznb(), obj, context, looper, zzf, this.zzaao, zza(api, i4)) : zza(api.zznc(), obj, context, looper, zzf, this.zzaao, zza(api, i4)));
        }
        this.zzZH = zzf;
        this.zzZj = zza;
    }

    private void resume() {
        this.zzZs.lock();
        try {
            if (zznC()) {
                connect();
            }
            this.zzZs.unlock();
        } catch (Throwable th) {
            this.zzZs.unlock();
        }
    }

    private static <C extends com.google.android.gms.common.api.Api.zzb, O> C zza(com.google.android.gms.common.api.Api.zza<C, O> zza, Object obj, Context context, Looper looper, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        return zza.zza(context, looper, zzf, obj, connectionCallbacks, onConnectionFailedListener);
    }

    private final OnConnectionFailedListener zza(final Api<?> api, final int i) {
        return new OnConnectionFailedListener(this) {
            final /* synthetic */ zzi zzaaq;

            public void onConnectionFailed(ConnectionResult result) {
                this.zzaaq.zzZs.lock();
                try {
                    this.zzaaq.zzaaj.zza(result, api, i);
                } finally {
                    this.zzaaq.zzZs.unlock();
                }
            }
        };
    }

    private static <C extends com.google.android.gms.common.api.Api.zzd, O> zzac zza(com.google.android.gms.common.api.Api.zze<C, O> zze, Object obj, Context context, Looper looper, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        return new zzac(context, looper, zze.zzng(), connectionCallbacks, onConnectionFailedListener, zzf, zze.zzm(obj));
    }

    private void zznD() {
        this.zzZs.lock();
        try {
            if (zznF()) {
                connect();
            }
            this.zzZs.unlock();
        } catch (Throwable th) {
            this.zzZs.unlock();
        }
    }

    public ConnectionResult blockingConnect(long timeout, TimeUnit unit) {
        zzx.zza(Looper.myLooper() != Looper.getMainLooper(), "blockingConnect must not be called on the UI thread");
        this.zzZs.lock();
        ConnectionResult connectionResult;
        try {
            connect();
            long toNanos = unit.toNanos(timeout);
            while (isConnecting()) {
                Object obj;
                toNanos = this.zzZY.awaitNanos(toNanos);
                if (toNanos > 0) {
                    obj = 1;
                    continue;
                } else {
                    obj = null;
                    continue;
                }
                if (obj == null) {
                    connectionResult = new ConnectionResult(14, null);
                    return connectionResult;
                }
            }
            if (isConnected()) {
                connectionResult = ConnectionResult.zzYi;
                this.zzZs.unlock();
                return connectionResult;
            } else if (this.zzaak == null) {
                connectionResult = new ConnectionResult(13, null);
                this.zzZs.unlock();
                return connectionResult;
            } else {
                connectionResult = this.zzaak;
                this.zzZs.unlock();
                return connectionResult;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connectionResult = new ConnectionResult(15, null);
            return connectionResult;
        } finally {
            this.zzZs.unlock();
        }
    }

    public void connect() {
        this.zzZs.lock();
        try {
            this.zzaaj.connect();
        } finally {
            this.zzZs.unlock();
        }
    }

    public void disconnect() {
        this.zzZs.lock();
        try {
            zznF();
            this.zzaaj.disconnect();
        } finally {
            this.zzZs.unlock();
        }
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.append(prefix).append("mState=").append(this.zzaaj.getName());
        writer.append(" mResuming=").print(this.zzaab);
        writer.append(" mWorkQueue.size()=").print(this.zzaaa.size());
        writer.append(" mUnconsumedRunners.size()=").println(this.zzaam.size());
        String str = prefix + "  ";
        for (Api api : this.zzZI.keySet()) {
            writer.append(prefix).append(api.getName()).println(":");
            ((com.google.android.gms.common.api.Api.zzb) this.zzaag.get(api.zznd())).dump(str, fd, writer, args);
        }
    }

    public Looper getLooper() {
        return this.zzYV;
    }

    public int getSessionId() {
        return System.identityHashCode(this);
    }

    public boolean isConnected() {
        return this.zzaaj instanceof zzf;
    }

    public boolean isConnecting() {
        return this.zzaaj instanceof zzg;
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        this.zzZZ.registerConnectionCallbacks(listener);
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        this.zzZZ.registerConnectionFailedListener(listener);
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        this.zzZZ.unregisterConnectionCallbacks(listener);
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        this.zzZZ.unregisterConnectionFailedListener(listener);
    }

    public <C extends com.google.android.gms.common.api.Api.zzb> C zza(com.google.android.gms.common.api.Api.zzc<C> zzc) {
        Object obj = (com.google.android.gms.common.api.Api.zzb) this.zzaag.get(zzc);
        zzx.zzb(obj, (Object) "Appropriate Api was not requested.");
        return obj;
    }

    public <A extends com.google.android.gms.common.api.Api.zzb, R extends Result, T extends com.google.android.gms.common.api.zzc.zza<R, A>> T zza(T t) {
        zzx.zzb(t.zznd() != null, (Object) "This task can not be enqueued (it's probably a Batch or malformed)");
        zzx.zzb(this.zzaag.containsKey(t.zznd()), (Object) "GoogleApiClient is not configured to use the API required for this call.");
        this.zzZs.lock();
        try {
            T zza = this.zzaaj.zza(t);
            return zza;
        } finally {
            this.zzZs.unlock();
        }
    }

    void zza(zzb zzb) {
        this.zzaae.sendMessage(this.zzaae.obtainMessage(3, zzb));
    }

    void zza(RuntimeException runtimeException) {
        this.zzaae.sendMessage(this.zzaae.obtainMessage(4, runtimeException));
    }

    <A extends com.google.android.gms.common.api.Api.zzb> void zzb(zze<A> zze) {
        this.zzaam.add(zze);
        zze.zza(this.zzaan);
    }

    void zzg(ConnectionResult connectionResult) {
        this.zzZs.lock();
        try {
            this.zzaak = connectionResult;
            this.zzaaj = new zzh(this);
            this.zzaaj.begin();
            this.zzZY.signalAll();
        } finally {
            this.zzZs.unlock();
        }
    }

    void zznA() {
        this.zzZs.lock();
        try {
            this.zzaaj = new zzg(this, this.zzZH, this.zzZI, this.zzZi, this.zzZj, this.zzZs, this.mContext);
            this.zzaaj.begin();
            this.zzZY.signalAll();
        } finally {
            this.zzZs.unlock();
        }
    }

    void zznB() {
        this.zzZs.lock();
        try {
            zznF();
            this.zzaaj = new zzf(this);
            this.zzaaj.begin();
            this.zzZY.signalAll();
        } finally {
            this.zzZs.unlock();
        }
    }

    boolean zznC() {
        return this.zzaab;
    }

    void zznE() {
        if (!zznC()) {
            this.zzaab = true;
            if (this.zzaaf == null) {
                this.zzaaf = new zzc(this);
                IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
                intentFilter.addDataScheme("package");
                this.mContext.getApplicationContext().registerReceiver(this.zzaaf, intentFilter);
            }
            this.zzaae.sendMessageDelayed(this.zzaae.obtainMessage(1), this.zzaac);
            this.zzaae.sendMessageDelayed(this.zzaae.obtainMessage(2), this.zzaad);
        }
    }

    boolean zznF() {
        if (!zznC()) {
            return false;
        }
        this.zzaab = false;
        this.zzaae.removeMessages(2);
        this.zzaae.removeMessages(1);
        if (this.zzaaf != null) {
            this.mContext.getApplicationContext().unregisterReceiver(this.zzaaf);
            this.zzaaf = null;
        }
        return true;
    }

    void zzny() {
        for (zze zze : this.zzaam) {
            zze.zza(null);
            zze.cancel();
        }
        this.zzaam.clear();
        for (zzl clear : this.zzaal) {
            clear.clear();
        }
        this.zzaal.clear();
    }

    void zznz() {
        for (com.google.android.gms.common.api.Api.zzb disconnect : this.zzaag.values()) {
            disconnect.disconnect();
        }
    }
}
