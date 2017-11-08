package com.google.android.gms.common.api.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.zza;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.Api.zzc;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzrn;
import com.google.android.gms.internal.zzro;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/* compiled from: Unknown */
public class zzd implements zzp {
    private final Context mContext;
    private final Lock zzXG;
    private final zzj zzagW;
    private final zzl zzagX;
    private final zzl zzagY;
    private final Map<zzc<?>, zzl> zzagZ = new ArrayMap();
    private final Looper zzagr;
    private final Set<zzu> zzaha = Collections.newSetFromMap(new WeakHashMap());
    private final zzb zzahb;
    private Bundle zzahc;
    private ConnectionResult zzahd = null;
    private ConnectionResult zzahe = null;
    private boolean zzahf = false;
    private int zzahg = 0;

    public zzd(Context context, zzj zzj, Lock lock, Looper looper, com.google.android.gms.common.zzc zzc, Map<zzc<?>, zzb> map, zzf zzf, Map<Api<?>, Integer> map2, zza<? extends zzrn, zzro> zza, ArrayList<zzc> arrayList) {
        this.mContext = context;
        this.zzagW = zzj;
        this.zzXG = lock;
        this.zzagr = looper;
        Map arrayMap = new ArrayMap();
        Map arrayMap2 = new ArrayMap();
        zzb zzb = null;
        for (zzc zzc2 : map.keySet()) {
            zzb zzb2 = (zzb) map.get(zzc2);
            if (zzb2.zznb()) {
                zzb = zzb2;
            }
            if (zzb2.zzmE()) {
                arrayMap.put(zzc2, zzb2);
            } else {
                arrayMap2.put(zzc2, zzb2);
            }
        }
        this.zzahb = zzb;
        if (arrayMap.isEmpty()) {
            throw new IllegalStateException("CompositeGoogleApiClient should not be used without any APIs that require sign-in.");
        }
        Map arrayMap3 = new ArrayMap();
        Map arrayMap4 = new ArrayMap();
        for (Api api : map2.keySet()) {
            zzc zzoR = api.zzoR();
            if (arrayMap.containsKey(zzoR)) {
                arrayMap3.put(api, map2.get(api));
            } else if (arrayMap2.containsKey(zzoR)) {
                arrayMap4.put(api, map2.get(api));
            } else {
                throw new IllegalStateException("Each API in the apiTypeMap must have a corresponding client in the clients map.");
            }
        }
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            zzc zzc3 = (zzc) it.next();
            if (arrayMap3.containsKey(zzc3.zzagT)) {
                arrayList2.add(zzc3);
            } else if (arrayMap4.containsKey(zzc3.zzagT)) {
                arrayList3.add(zzc3);
            } else {
                throw new IllegalStateException("Each ClientCallbacks must have a corresponding API in the apiTypeMap");
            }
        }
        Context context2 = context;
        this.zzagX = new zzl(context2, this.zzagW, lock, looper, zzc, arrayMap2, null, arrayMap4, null, arrayList3, new zzp.zza(this) {
            final /* synthetic */ zzd zzahh;

            {
                this.zzahh = r1;
            }

            public void zzc(int i, boolean z) {
                this.zzahh.zzXG.lock();
                try {
                    if (!this.zzahh.zzahf) {
                        if (this.zzahh.zzahe != null && this.zzahh.zzahe.isSuccess()) {
                            this.zzahh.zzahf = true;
                            this.zzahh.zzagY.onConnectionSuspended(i);
                            this.zzahh.zzXG.unlock();
                            return;
                        }
                    }
                    this.zzahh.zzahf = false;
                    this.zzahh.zzb(i, z);
                } finally {
                    this.zzahh.zzXG.unlock();
                }
            }

            public void zzd(@NonNull ConnectionResult connectionResult) {
                this.zzahh.zzXG.lock();
                try {
                    this.zzahh.zzahd = connectionResult;
                    this.zzahh.zzpm();
                } finally {
                    this.zzahh.zzXG.unlock();
                }
            }

            public void zzi(@Nullable Bundle bundle) {
                this.zzahh.zzXG.lock();
                try {
                    this.zzahh.zzh(bundle);
                    this.zzahh.zzahd = ConnectionResult.zzafB;
                    this.zzahh.zzpm();
                } finally {
                    this.zzahh.zzXG.unlock();
                }
            }
        });
        Context context3 = context;
        this.zzagY = new zzl(context3, this.zzagW, lock, looper, zzc, arrayMap, zzf, arrayMap3, zza, arrayList2, new zzp.zza(this) {
            final /* synthetic */ zzd zzahh;

            {
                this.zzahh = r1;
            }

            public void zzc(int i, boolean z) {
                this.zzahh.zzXG.lock();
                try {
                    if (this.zzahh.zzahf) {
                        this.zzahh.zzahf = false;
                        this.zzahh.zzb(i, z);
                        this.zzahh.zzXG.unlock();
                        return;
                    }
                    this.zzahh.zzahf = true;
                    this.zzahh.zzagX.onConnectionSuspended(i);
                } finally {
                    this.zzahh.zzXG.unlock();
                }
            }

            public void zzd(@NonNull ConnectionResult connectionResult) {
                this.zzahh.zzXG.lock();
                try {
                    this.zzahh.zzahe = connectionResult;
                    this.zzahh.zzpm();
                } finally {
                    this.zzahh.zzXG.unlock();
                }
            }

            public void zzi(@Nullable Bundle bundle) {
                this.zzahh.zzXG.lock();
                try {
                    this.zzahh.zzahe = ConnectionResult.zzafB;
                    this.zzahh.zzpm();
                } finally {
                    this.zzahh.zzXG.unlock();
                }
            }
        });
        for (zzc zzc22 : arrayMap2.keySet()) {
            this.zzagZ.put(zzc22, this.zzagX);
        }
        for (zzc zzc222 : arrayMap.keySet()) {
            this.zzagZ.put(zzc222, this.zzagY);
        }
    }

    private void zzb(int i, boolean z) {
        this.zzagW.zzc(i, z);
        this.zzahe = null;
        this.zzahd = null;
    }

    private void zzb(ConnectionResult connectionResult) {
        switch (this.zzahg) {
            case 1:
                break;
            case 2:
                this.zzagW.zzd(connectionResult);
                break;
            default:
                Log.wtf("CompositeGAC", "Attempted to call failure callbacks in CONNECTION_MODE_NONE. Callbacks should be disabled via GmsClientSupervisor", new Exception());
                break;
        }
        zzpo();
        this.zzahg = 0;
    }

    private static boolean zzc(ConnectionResult connectionResult) {
        return connectionResult != null && connectionResult.isSuccess();
    }

    private boolean zzc(zza.zza<? extends Result, ? extends zzb> zza) {
        zzc zzoR = zza.zzoR();
        zzx.zzb(this.zzagZ.containsKey(zzoR), (Object) "GoogleApiClient is not configured to use the API required for this call.");
        return ((zzl) this.zzagZ.get(zzoR)).equals(this.zzagY);
    }

    private void zzh(Bundle bundle) {
        if (this.zzahc == null) {
            this.zzahc = bundle;
        } else if (bundle != null) {
            this.zzahc.putAll(bundle);
        }
    }

    private void zzpl() {
        this.zzahe = null;
        this.zzahd = null;
        this.zzagX.connect();
        this.zzagY.connect();
    }

    private void zzpm() {
        if (zzc(this.zzahd)) {
            if (zzc(this.zzahe) || zzpp()) {
                zzpn();
            } else if (this.zzahe == null) {
            } else {
                if (this.zzahg != 1) {
                    zzb(this.zzahe);
                    this.zzagX.disconnect();
                    return;
                }
                zzpo();
            }
        } else if (this.zzahd != null && zzc(this.zzahe)) {
            this.zzagY.disconnect();
            zzb(this.zzahd);
        } else if (this.zzahd != null && this.zzahe != null) {
            ConnectionResult connectionResult = this.zzahd;
            if (this.zzagY.zzair < this.zzagX.zzair) {
                connectionResult = this.zzahe;
            }
            zzb(connectionResult);
        }
    }

    private void zzpn() {
        switch (this.zzahg) {
            case 1:
                break;
            case 2:
                this.zzagW.zzi(this.zzahc);
                break;
            default:
                Log.wtf("CompositeGAC", "Attempted to call success callbacks in CONNECTION_MODE_NONE. Callbacks should be disabled via GmsClientSupervisor", new Exception());
                break;
        }
        zzpo();
        this.zzahg = 0;
    }

    private void zzpo() {
        for (zzu zzna : this.zzaha) {
            zzna.zzna();
        }
        this.zzaha.clear();
    }

    private boolean zzpp() {
        return this.zzahe != null && this.zzahe.getErrorCode() == 4;
    }

    @Nullable
    private PendingIntent zzpq() {
        return this.zzahb != null ? PendingIntent.getActivity(this.mContext, this.zzagW.getSessionId(), this.zzahb.zznc(), 134217728) : null;
    }

    public ConnectionResult blockingConnect() {
        throw new UnsupportedOperationException();
    }

    public ConnectionResult blockingConnect(long timeout, @NonNull TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    public void connect() {
        this.zzahg = 2;
        this.zzahf = false;
        zzpl();
    }

    public boolean disconnect() {
        this.zzahe = null;
        this.zzahd = null;
        this.zzahg = 0;
        boolean disconnect = this.zzagX.disconnect();
        boolean disconnect2 = this.zzagY.disconnect();
        zzpo();
        return disconnect && disconnect2;
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.append(prefix).append("authClient").println(":");
        this.zzagY.dump(prefix + "  ", fd, writer, args);
        writer.append(prefix).append("anonClient").println(":");
        this.zzagX.dump(prefix + "  ", fd, writer, args);
    }

    @Nullable
    public ConnectionResult getConnectionResult(@NonNull Api<?> api) {
        return !((zzl) this.zzagZ.get(api.zzoR())).equals(this.zzagY) ? this.zzagX.getConnectionResult(api) : !zzpp() ? this.zzagY.getConnectionResult(api) : new ConnectionResult(4, zzpq());
    }

    public boolean isConnected() {
        boolean z = true;
        this.zzXG.lock();
        try {
            if (this.zzagX.isConnected()) {
                if (!(zzpk() || zzpp() || this.zzahg == 1)) {
                }
                this.zzXG.unlock();
                return z;
            }
            z = false;
            this.zzXG.unlock();
            return z;
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    public boolean isConnecting() {
        this.zzXG.lock();
        try {
            boolean z = this.zzahg == 2;
            this.zzXG.unlock();
            return z;
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    public <A extends zzb, R extends Result, T extends zza.zza<R, A>> T zza(@NonNull T t) {
        if (!zzc((zza.zza) t)) {
            return this.zzagX.zza((zza.zza) t);
        }
        if (!zzpp()) {
            return this.zzagY.zza((zza.zza) t);
        }
        t.zzw(new Status(4, null, zzpq()));
        return t;
    }

    public boolean zza(zzu zzu) {
        this.zzXG.lock();
        try {
            if (!isConnecting()) {
                if (!isConnected()) {
                    this.zzXG.unlock();
                    return false;
                }
            }
            if (!zzpk()) {
                this.zzaha.add(zzu);
                if (this.zzahg == 0) {
                    this.zzahg = 1;
                }
                this.zzahe = null;
                this.zzagY.connect();
                this.zzXG.unlock();
                return true;
            }
            this.zzXG.unlock();
            return false;
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    public <A extends zzb, T extends zza.zza<? extends Result, A>> T zzb(@NonNull T t) {
        if (!zzc((zza.zza) t)) {
            return this.zzagX.zzb((zza.zza) t);
        }
        if (!zzpp()) {
            return this.zzagY.zzb((zza.zza) t);
        }
        t.zzw(new Status(4, null, zzpq()));
        return t;
    }

    public void zzoW() {
        this.zzXG.lock();
        try {
            boolean isConnecting = isConnecting();
            this.zzagY.disconnect();
            this.zzahe = new ConnectionResult(4);
            if (isConnecting) {
                new Handler(this.zzagr).post(new Runnable(this) {
                    final /* synthetic */ zzd zzahh;

                    {
                        this.zzahh = r1;
                    }

                    public void run() {
                        this.zzahh.zzXG.lock();
                        try {
                            this.zzahh.zzpm();
                        } finally {
                            this.zzahh.zzXG.unlock();
                        }
                    }
                });
            } else {
                zzpo();
            }
            this.zzXG.unlock();
        } catch (Throwable th) {
            this.zzXG.unlock();
        }
    }

    public void zzpj() {
        this.zzagX.zzpj();
        this.zzagY.zzpj();
    }

    public boolean zzpk() {
        return this.zzagY.isConnected();
    }
}
