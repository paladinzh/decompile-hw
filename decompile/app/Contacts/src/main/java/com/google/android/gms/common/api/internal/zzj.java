package com.google.android.gms.common.api.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.google.android.gms.auth.api.signin.internal.zzq;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzk;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzmf;
import com.google.android.gms.internal.zzrn;
import com.google.android.gms.internal.zzro;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

/* compiled from: Unknown */
public final class zzj extends GoogleApiClient implements com.google.android.gms.common.api.internal.zzp.zza {
    private final Context mContext;
    private final Lock zzXG;
    private final int zzagp;
    private final Looper zzagr;
    private final com.google.android.gms.common.zzc zzags;
    final com.google.android.gms.common.api.Api.zza<? extends zzrn, zzro> zzagt;
    final Map<Api<?>, Integer> zzahA;
    private final zzk zzahL;
    private zzp zzahM = null;
    final Queue<com.google.android.gms.common.api.internal.zza.zza<?, ?>> zzahN = new LinkedList();
    private volatile boolean zzahO;
    private long zzahP = 120000;
    private long zzahQ = 5000;
    private final zza zzahR;
    zzc zzahS;
    final Map<com.google.android.gms.common.api.Api.zzc<?>, com.google.android.gms.common.api.Api.zzb> zzahT;
    Set<Scope> zzahU = new HashSet();
    private final Set<zzq<?>> zzahV = Collections.newSetFromMap(new WeakHashMap());
    final Set<zze<?>> zzahW = Collections.newSetFromMap(new ConcurrentHashMap(16, 0.75f, 2));
    private com.google.android.gms.common.api.zza zzahX;
    private final ArrayList<zzc> zzahY;
    private Integer zzahZ = null;
    final zzf zzahz;
    Set<zzx> zzaia = null;
    private final zzd zzaib = new zzd(this) {
        final /* synthetic */ zzj zzaid;

        {
            this.zzaid = r1;
        }

        public void zzc(zze<?> zze) {
            this.zzaid.zzahW.remove(zze);
            if (zze.zzpa() != null && this.zzaid.zzahX != null) {
                this.zzaid.zzahX.remove(zze.zzpa().intValue());
            }
        }
    };
    private final com.google.android.gms.common.internal.zzk.zza zzaic = new com.google.android.gms.common.internal.zzk.zza(this) {
        final /* synthetic */ zzj zzaid;

        {
            this.zzaid = r1;
        }

        public boolean isConnected() {
            return this.zzaid.isConnected();
        }

        public Bundle zzoi() {
            return null;
        }
    };

    /* compiled from: Unknown */
    interface zze<A extends com.google.android.gms.common.api.Api.zzb> {
        void cancel();

        boolean isReady();

        void zza(zzd zzd);

        void zzb(A a) throws DeadObjectException;

        com.google.android.gms.common.api.Api.zzc<A> zzoR();

        Integer zzpa();

        void zzpe();

        void zzpg();

        void zzw(Status status);

        void zzx(Status status);
    }

    /* compiled from: Unknown */
    interface zzd {
        void zzc(zze<?> zze);
    }

    /* compiled from: Unknown */
    final class zza extends Handler {
        final /* synthetic */ zzj zzaid;

        zza(zzj zzj, Looper looper) {
            this.zzaid = zzj;
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    this.zzaid.zzpD();
                    return;
                case 2:
                    this.zzaid.resume();
                    return;
                default:
                    Log.w("GoogleApiClientImpl", "Unknown message id: " + msg.what);
                    return;
            }
        }
    }

    /* compiled from: Unknown */
    private static class zzb implements DeathRecipient, zzd {
        private final WeakReference<zze<?>> zzaii;
        private final WeakReference<com.google.android.gms.common.api.zza> zzaij;
        private final WeakReference<IBinder> zzaik;

        private zzb(zze zze, com.google.android.gms.common.api.zza zza, IBinder iBinder) {
            this.zzaij = new WeakReference(zza);
            this.zzaii = new WeakReference(zze);
            this.zzaik = new WeakReference(iBinder);
        }

        private void zzpI() {
            zze zze = (zze) this.zzaii.get();
            com.google.android.gms.common.api.zza zza = (com.google.android.gms.common.api.zza) this.zzaij.get();
            if (!(zza == null || zze == null)) {
                zza.remove(zze.zzpa().intValue());
            }
            IBinder iBinder = (IBinder) this.zzaik.get();
            if (this.zzaik != null) {
                iBinder.unlinkToDeath(this, 0);
            }
        }

        public void binderDied() {
            zzpI();
        }

        public void zzc(zze<?> zze) {
            zzpI();
        }
    }

    /* compiled from: Unknown */
    static class zzc extends zzn {
        private WeakReference<zzj> zzail;

        zzc(zzj zzj) {
            this.zzail = new WeakReference(zzj);
        }

        public void zzpJ() {
            zzj zzj = (zzj) this.zzail.get();
            if (zzj != null) {
                zzj.resume();
            }
        }
    }

    public zzj(Context context, Lock lock, Looper looper, zzf zzf, com.google.android.gms.common.zzc zzc, com.google.android.gms.common.api.Api.zza<? extends zzrn, zzro> zza, Map<Api<?>, Integer> map, List<ConnectionCallbacks> list, List<OnConnectionFailedListener> list2, Map<com.google.android.gms.common.api.Api.zzc<?>, com.google.android.gms.common.api.Api.zzb> map2, int i, int i2, ArrayList<zzc> arrayList) {
        this.mContext = context;
        this.zzXG = lock;
        this.zzahL = new zzk(looper, this.zzaic);
        this.zzagr = looper;
        this.zzahR = new zza(this, looper);
        this.zzags = zzc;
        this.zzagp = i;
        if (this.zzagp >= 0) {
            this.zzahZ = Integer.valueOf(i2);
        }
        this.zzahA = map;
        this.zzahT = map2;
        this.zzahY = arrayList;
        for (ConnectionCallbacks registerConnectionCallbacks : list) {
            this.zzahL.registerConnectionCallbacks(registerConnectionCallbacks);
        }
        for (OnConnectionFailedListener registerConnectionFailedListener : list2) {
            this.zzahL.registerConnectionFailedListener(registerConnectionFailedListener);
        }
        this.zzahz = zzf;
        this.zzagt = zza;
    }

    private void resume() {
        this.zzXG.lock();
        try {
            if (zzpB()) {
                zzpC();
            }
            this.zzXG.unlock();
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    public static int zza(Iterable<com.google.android.gms.common.api.Api.zzb> iterable, boolean z) {
        int i = 0;
        int i2 = 0;
        for (com.google.android.gms.common.api.Api.zzb zzb : iterable) {
            if (zzb.zzmE()) {
                i2 = 1;
            }
            i = !zzb.zznb() ? i : 1;
        }
        return i2 == 0 ? 3 : (i != 0 && z) ? 2 : 1;
    }

    private void zza(final GoogleApiClient googleApiClient, final zzv zzv, final boolean z) {
        zzmf.zzamA.zzf(googleApiClient).setResultCallback(new ResultCallback<Status>(this) {
            final /* synthetic */ zzj zzaid;

            public /* synthetic */ void onResult(@NonNull Result result) {
                zzp((Status) result);
            }

            public void zzp(@NonNull Status status) {
                zzq.zzaf(this.zzaid.mContext).zznr();
                if (status.isSuccess() && this.zzaid.isConnected()) {
                    this.zzaid.reconnect();
                }
                zzv.zza((Result) status);
                if (z) {
                    googleApiClient.disconnect();
                }
            }
        });
    }

    private static void zza(zze<?> zze, com.google.android.gms.common.api.zza zza, IBinder iBinder) {
        if (zze.isReady()) {
            zze.zza(new zzb(zze, zza, iBinder));
        } else if (iBinder != null && iBinder.isBinderAlive()) {
            Object zzb = new zzb(zze, zza, iBinder);
            zze.zza(zzb);
            try {
                iBinder.linkToDeath(zzb, 0);
            } catch (RemoteException e) {
                zze.cancel();
                zza.remove(zze.zzpa().intValue());
            }
        } else {
            zze.zza(null);
            zze.cancel();
            zza.remove(zze.zzpa().intValue());
        }
    }

    private void zzbB(int i) {
        if (this.zzahZ == null) {
            this.zzahZ = Integer.valueOf(i);
        } else if (this.zzahZ.intValue() != i) {
            throw new IllegalStateException("Cannot use sign-in mode: " + zzbC(i) + ". Mode was already set to " + zzbC(this.zzahZ.intValue()));
        }
        if (this.zzahM == null) {
            Object obj = null;
            Object obj2 = null;
            for (com.google.android.gms.common.api.Api.zzb zzb : this.zzahT.values()) {
                Object obj3;
                if (zzb.zzmE()) {
                    int i2 = 1;
                }
                if (zzb.zznb()) {
                    int i3 = 1;
                } else {
                    obj3 = obj;
                }
                obj = obj3;
            }
            switch (this.zzahZ.intValue()) {
                case 1:
                    if (obj2 == null) {
                        throw new IllegalStateException("SIGN_IN_MODE_REQUIRED cannot be used on a GoogleApiClient that does not contain any authenticated APIs. Use connect() instead.");
                    } else if (obj != null) {
                        throw new IllegalStateException("Cannot use SIGN_IN_MODE_REQUIRED with GOOGLE_SIGN_IN_API. Use connect(SIGN_IN_MODE_OPTIONAL) instead.");
                    }
                    break;
                case 2:
                    if (obj2 != null) {
                        this.zzahM = new zzd(this.mContext, this, this.zzXG, this.zzagr, this.zzags, this.zzahT, this.zzahz, this.zzahA, this.zzagt, this.zzahY);
                        return;
                    }
                    break;
            }
            this.zzahM = new zzl(this.mContext, this, this.zzXG, this.zzagr, this.zzags, this.zzahT, this.zzahz, this.zzahA, this.zzagt, this.zzahY, this);
        }
    }

    static String zzbC(int i) {
        switch (i) {
            case 1:
                return "SIGN_IN_MODE_REQUIRED";
            case 2:
                return "SIGN_IN_MODE_OPTIONAL";
            case 3:
                return "SIGN_IN_MODE_NONE";
            default:
                return "UNKNOWN";
        }
    }

    private void zzpC() {
        this.zzahL.zzqR();
        this.zzahM.connect();
    }

    private void zzpD() {
        this.zzXG.lock();
        try {
            if (zzpF()) {
                zzpC();
            }
            this.zzXG.unlock();
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    public ConnectionResult blockingConnect() {
        boolean z = false;
        zzx.zza(Looper.myLooper() != Looper.getMainLooper(), (Object) "blockingConnect must not be called on the UI thread");
        this.zzXG.lock();
        try {
            if (this.zzagp >= 0) {
                if (this.zzahZ != null) {
                    z = true;
                }
                zzx.zza(z, (Object) "Sign-in mode should have been set explicitly by auto-manage.");
            } else if (this.zzahZ == null) {
                this.zzahZ = Integer.valueOf(zza(this.zzahT.values(), false));
            } else if (this.zzahZ.intValue() == 2) {
                throw new IllegalStateException("Cannot call blockingConnect() when sign-in mode is set to SIGN_IN_MODE_OPTIONAL. Call connect(SIGN_IN_MODE_OPTIONAL) instead.");
            }
            zzbB(this.zzahZ.intValue());
            this.zzahL.zzqR();
            ConnectionResult blockingConnect = this.zzahM.blockingConnect();
            return blockingConnect;
        } finally {
            this.zzXG.unlock();
        }
    }

    public ConnectionResult blockingConnect(long timeout, @NonNull TimeUnit unit) {
        boolean z = false;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            z = true;
        }
        zzx.zza(z, (Object) "blockingConnect must not be called on the UI thread");
        zzx.zzb((Object) unit, (Object) "TimeUnit must not be null");
        this.zzXG.lock();
        try {
            if (this.zzahZ == null) {
                this.zzahZ = Integer.valueOf(zza(this.zzahT.values(), false));
            } else if (this.zzahZ.intValue() == 2) {
                throw new IllegalStateException("Cannot call blockingConnect() when sign-in mode is set to SIGN_IN_MODE_OPTIONAL. Call connect(SIGN_IN_MODE_OPTIONAL) instead.");
            }
            zzbB(this.zzahZ.intValue());
            this.zzahL.zzqR();
            ConnectionResult blockingConnect = this.zzahM.blockingConnect(timeout, unit);
            return blockingConnect;
        } finally {
            this.zzXG.unlock();
        }
    }

    public PendingResult<Status> clearDefaultAccountAndReconnect() {
        zzx.zza(isConnected(), (Object) "GoogleApiClient is not connected yet.");
        zzx.zza(this.zzahZ.intValue() != 2, (Object) "Cannot use clearDefaultAccountAndReconnect with GOOGLE_SIGN_IN_API");
        final zzv zzv = new zzv((GoogleApiClient) this);
        if (this.zzahT.containsKey(zzmf.zzUI)) {
            zza((GoogleApiClient) this, zzv, false);
        } else {
            final AtomicReference atomicReference = new AtomicReference();
            GoogleApiClient build = new Builder(this.mContext).addApi(zzmf.API).addConnectionCallbacks(new ConnectionCallbacks(this) {
                final /* synthetic */ zzj zzaid;

                public void onConnected(Bundle connectionHint) {
                    this.zzaid.zza((GoogleApiClient) atomicReference.get(), zzv, true);
                }

                public void onConnectionSuspended(int cause) {
                }
            }).addOnConnectionFailedListener(new OnConnectionFailedListener(this) {
                final /* synthetic */ zzj zzaid;

                public void onConnectionFailed(@NonNull ConnectionResult result) {
                    zzv.zza(new Status(8));
                }
            }).setHandler(this.zzahR).build();
            atomicReference.set(build);
            build.connect();
        }
        return zzv;
    }

    public void connect() {
        boolean z = false;
        this.zzXG.lock();
        try {
            if (this.zzagp >= 0) {
                if (this.zzahZ != null) {
                    z = true;
                }
                zzx.zza(z, (Object) "Sign-in mode should have been set explicitly by auto-manage.");
            } else if (this.zzahZ == null) {
                this.zzahZ = Integer.valueOf(zza(this.zzahT.values(), false));
            } else if (this.zzahZ.intValue() == 2) {
                throw new IllegalStateException("Cannot call connect() when SignInMode is set to SIGN_IN_MODE_OPTIONAL. Call connect(SIGN_IN_MODE_OPTIONAL) instead.");
            }
            connect(this.zzahZ.intValue());
        } finally {
            this.zzXG.unlock();
        }
    }

    public void connect(int signInMode) {
        boolean z = true;
        this.zzXG.lock();
        if (!(signInMode == 3 || signInMode == 1 || signInMode == 2)) {
            z = false;
        }
        try {
            zzx.zzb(z, "Illegal sign-in mode: " + signInMode);
            zzbB(signInMode);
            zzpC();
        } finally {
            this.zzXG.unlock();
        }
    }

    public void disconnect() {
        boolean z = false;
        this.zzXG.lock();
        try {
            if (this.zzahM != null) {
                if (!this.zzahM.disconnect()) {
                    z = true;
                }
            }
            zzaa(z);
            for (zzq clear : this.zzahV) {
                clear.clear();
            }
            this.zzahV.clear();
            for (zze zze : this.zzahN) {
                zze.zza(null);
                zze.cancel();
            }
            this.zzahN.clear();
            if (this.zzahM != null) {
                zzpF();
                this.zzahL.zzqQ();
                return;
            }
            this.zzXG.unlock();
        } finally {
            this.zzXG.unlock();
        }
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.append(prefix).append("mContext=").println(this.mContext);
        writer.append(prefix).append("mResuming=").print(this.zzahO);
        writer.append(" mWorkQueue.size()=").print(this.zzahN.size());
        writer.append(" mUnconsumedRunners.size()=").println(this.zzahW.size());
        if (this.zzahM != null) {
            this.zzahM.dump(prefix, fd, writer, args);
        }
    }

    @NonNull
    public ConnectionResult getConnectionResult(@NonNull Api<?> api) {
        this.zzXG.lock();
        try {
            if (!isConnected()) {
                if (!zzpB()) {
                    throw new IllegalStateException("Cannot invoke getConnectionResult unless GoogleApiClient is connected");
                }
            }
            if (this.zzahT.containsKey(api.zzoR())) {
                ConnectionResult connectionResult = this.zzahM.getConnectionResult(api);
                if (connectionResult != null) {
                    return connectionResult;
                }
                if (zzpB()) {
                    connectionResult = ConnectionResult.zzafB;
                    this.zzXG.unlock();
                    return connectionResult;
                }
                Log.i("GoogleApiClientImpl", zzpH());
                Log.wtf("GoogleApiClientImpl", api.getName() + " requested in getConnectionResult" + " is not connected but is not present in the failed " + " connections map", new Exception());
                connectionResult = new ConnectionResult(8, null);
                this.zzXG.unlock();
                return connectionResult;
            }
            throw new IllegalArgumentException(api.getName() + " was never registered with GoogleApiClient");
        } finally {
            this.zzXG.unlock();
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public Looper getLooper() {
        return this.zzagr;
    }

    public int getSessionId() {
        return System.identityHashCode(this);
    }

    public boolean hasConnectedApi(@NonNull Api<?> api) {
        com.google.android.gms.common.api.Api.zzb zzb = (com.google.android.gms.common.api.Api.zzb) this.zzahT.get(api.zzoR());
        return zzb != null && zzb.isConnected();
    }

    public boolean isConnected() {
        return this.zzahM != null && this.zzahM.isConnected();
    }

    public boolean isConnecting() {
        return this.zzahM != null && this.zzahM.isConnecting();
    }

    public boolean isConnectionCallbacksRegistered(@NonNull ConnectionCallbacks listener) {
        return this.zzahL.isConnectionCallbacksRegistered(listener);
    }

    public boolean isConnectionFailedListenerRegistered(@NonNull OnConnectionFailedListener listener) {
        return this.zzahL.isConnectionFailedListenerRegistered(listener);
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public void registerConnectionCallbacks(@NonNull ConnectionCallbacks listener) {
        this.zzahL.registerConnectionCallbacks(listener);
    }

    public void registerConnectionFailedListener(@NonNull OnConnectionFailedListener listener) {
        this.zzahL.registerConnectionFailedListener(listener);
    }

    public void stopAutoManage(@NonNull final FragmentActivity lifecycleActivity) {
        if (this.zzagp < 0) {
            throw new IllegalStateException("Called stopAutoManage but automatic lifecycle management is not enabled.");
        }
        zzw zza = zzw.zza(lifecycleActivity);
        if (zza != null) {
            zza.zzbD(this.zzagp);
        } else {
            new Handler(this.mContext.getMainLooper()).post(new Runnable(this) {
                final /* synthetic */ zzj zzaid;

                public void run() {
                    if (!lifecycleActivity.isFinishing() && !lifecycleActivity.getSupportFragmentManager().isDestroyed()) {
                        zzw.zzb(lifecycleActivity).zzbD(this.zzaid.zzagp);
                    }
                }
            });
        }
    }

    public void unregisterConnectionCallbacks(@NonNull ConnectionCallbacks listener) {
        this.zzahL.unregisterConnectionCallbacks(listener);
    }

    public void unregisterConnectionFailedListener(@NonNull OnConnectionFailedListener listener) {
        this.zzahL.unregisterConnectionFailedListener(listener);
    }

    @NonNull
    public <C extends com.google.android.gms.common.api.Api.zzb> C zza(@NonNull com.google.android.gms.common.api.Api.zzc<C> zzc) {
        Object obj = (com.google.android.gms.common.api.Api.zzb) this.zzahT.get(zzc);
        zzx.zzb(obj, (Object) "Appropriate Api was not requested.");
        return obj;
    }

    public <A extends com.google.android.gms.common.api.Api.zzb, R extends Result, T extends com.google.android.gms.common.api.internal.zza.zza<R, A>> T zza(@NonNull T t) {
        zzx.zzb(t.zzoR() != null, (Object) "This task can not be enqueued (it's probably a Batch or malformed)");
        zzx.zzb(this.zzahT.containsKey(t.zzoR()), (Object) "GoogleApiClient is not configured to use the API required for this call.");
        this.zzXG.lock();
        try {
            if (this.zzahM != null) {
                T zza = this.zzahM.zza((com.google.android.gms.common.api.internal.zza.zza) t);
                return zza;
            }
            this.zzahN.add(t);
            this.zzXG.unlock();
            return t;
        } finally {
            this.zzXG.unlock();
        }
    }

    public void zza(zzx zzx) {
        this.zzXG.lock();
        try {
            if (this.zzaia == null) {
                this.zzaia = new HashSet();
            }
            this.zzaia.add(zzx);
        } finally {
            this.zzXG.unlock();
        }
    }

    public boolean zza(@NonNull Api<?> api) {
        return this.zzahT.containsKey(api.zzoR());
    }

    public boolean zza(zzu zzu) {
        return this.zzahM != null && this.zzahM.zza(zzu);
    }

    void zzaa(boolean z) {
        for (zze zze : this.zzahW) {
            if (zze.zzpa() != null) {
                zze.zzpe();
                zza(zze, this.zzahX, zza(zze.zzoR()).zzoT());
                this.zzahW.remove(zze);
            } else if (z) {
                zze.zzpg();
            } else {
                zze.cancel();
                this.zzahW.remove(zze);
            }
        }
    }

    public <A extends com.google.android.gms.common.api.Api.zzb, T extends com.google.android.gms.common.api.internal.zza.zza<? extends Result, A>> T zzb(@NonNull T t) {
        boolean z = false;
        if (t.zzoR() != null) {
            z = true;
        }
        zzx.zzb(z, (Object) "This task can not be executed (it's probably a Batch or malformed)");
        this.zzXG.lock();
        try {
            if (this.zzahM == null) {
                throw new IllegalStateException("GoogleApiClient is not connected yet.");
            } else if (zzpB()) {
                this.zzahN.add(t);
                while (!this.zzahN.isEmpty()) {
                    zze zze = (zze) this.zzahN.remove();
                    zzb(zze);
                    zze.zzw(Status.zzagE);
                }
                this.zzXG.unlock();
                return t;
            } else {
                T zzb = this.zzahM.zzb(t);
                return zzb;
            }
        } finally {
            this.zzXG.unlock();
        }
    }

    <A extends com.google.android.gms.common.api.Api.zzb> void zzb(zze<A> zze) {
        this.zzahW.add(zze);
        zze.zza(this.zzaib);
    }

    public void zzb(zzx zzx) {
        this.zzXG.lock();
        try {
            if (this.zzaia == null) {
                Log.wtf("GoogleApiClientImpl", "Attempted to remove pending transform when no transforms are registered.", new Exception());
            } else if (!this.zzaia.remove(zzx)) {
                Log.wtf("GoogleApiClientImpl", "Failed to remove pending transform - this may lead to memory leaks!", new Exception());
            } else if (!zzpG()) {
                this.zzahM.zzpj();
            }
            this.zzXG.unlock();
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    public void zzc(int i, boolean z) {
        if (i == 1 && !z) {
            zzpE();
        }
        for (zze zze : this.zzahW) {
            if (z) {
                zze.zzpe();
            }
            zze.zzx(new Status(8, "The connection to Google Play services was lost"));
        }
        this.zzahW.clear();
        this.zzahL.zzbT(i);
        this.zzahL.zzqQ();
        if (i == 2) {
            zzpC();
        }
    }

    public void zzd(ConnectionResult connectionResult) {
        if (!this.zzags.zzd(this.mContext, connectionResult.getErrorCode())) {
            zzpF();
        }
        if (!zzpB()) {
            this.zzahL.zzk(connectionResult);
            this.zzahL.zzqQ();
        }
    }

    public void zzi(Bundle bundle) {
        while (!this.zzahN.isEmpty()) {
            zzb((com.google.android.gms.common.api.internal.zza.zza) this.zzahN.remove());
        }
        this.zzahL.zzk(bundle);
    }

    public void zzoW() {
        if (this.zzahM != null) {
            this.zzahM.zzoW();
        }
    }

    boolean zzpB() {
        return this.zzahO;
    }

    void zzpE() {
        if (!zzpB()) {
            this.zzahO = true;
            if (this.zzahS == null) {
                this.zzahS = (zzc) zzn.zza(this.mContext.getApplicationContext(), new zzc(this), this.zzags);
            }
            this.zzahR.sendMessageDelayed(this.zzahR.obtainMessage(1), this.zzahP);
            this.zzahR.sendMessageDelayed(this.zzahR.obtainMessage(2), this.zzahQ);
        }
    }

    boolean zzpF() {
        if (!zzpB()) {
            return false;
        }
        this.zzahO = false;
        this.zzahR.removeMessages(2);
        this.zzahR.removeMessages(1);
        if (this.zzahS != null) {
            this.zzahS.unregister();
            this.zzahS = null;
        }
        return true;
    }

    boolean zzpG() {
        boolean z = false;
        this.zzXG.lock();
        try {
            if (this.zzaia != null) {
                if (!this.zzaia.isEmpty()) {
                    z = true;
                }
                this.zzXG.unlock();
                return z;
            }
            this.zzXG.unlock();
            return false;
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    String zzpH() {
        Writer stringWriter = new StringWriter();
        dump("", null, new PrintWriter(stringWriter), null);
        return stringWriter.toString();
    }

    public <L> zzq<L> zzr(@NonNull L l) {
        zzx.zzb((Object) l, (Object) "Listener must not be null");
        this.zzXG.lock();
        try {
            zzq<L> zzq = new zzq(this.zzagr, l);
            this.zzahV.add(zzq);
            return zzq;
        } finally {
            this.zzXG.unlock();
        }
    }
}
