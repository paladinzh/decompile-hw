package com.google.android.gms.common.api;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzp;
import com.google.android.gms.common.internal.zzx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public final class Api<O extends ApiOptions> {
    private final String mName;
    private final zzc<?> zzaeE;
    private final zza<?, O> zzafW;
    private final zze<?, O> zzafX = null;
    private final zzf<?> zzafY;

    /* compiled from: Unknown */
    public interface ApiOptions {

        /* compiled from: Unknown */
        public interface HasOptions extends ApiOptions {
        }

        /* compiled from: Unknown */
        public interface NotRequiredOptions extends ApiOptions {
        }

        /* compiled from: Unknown */
        public interface Optional extends HasOptions, NotRequiredOptions {
        }

        /* compiled from: Unknown */
        public static final class NoOptions implements NotRequiredOptions {
            private NoOptions() {
            }
        }
    }

    /* compiled from: Unknown */
    public static abstract class zza<T extends zzb, O> {
        public int getPriority() {
            return Integer.MAX_VALUE;
        }

        public abstract T zza(Context context, Looper looper, com.google.android.gms.common.internal.zzf zzf, O o, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener);

        public List<Scope> zzo(O o) {
            return Collections.emptyList();
        }
    }

    /* compiled from: Unknown */
    public interface zzb {
        void disconnect();

        void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

        boolean isConnected();

        void zza(com.google.android.gms.common.api.GoogleApiClient.zza zza);

        void zza(zzp zzp, Set<Scope> set);

        boolean zzmE();

        boolean zznb();

        Intent zznc();

        IBinder zzoT();
    }

    /* compiled from: Unknown */
    public static final class zzc<C extends zzb> {
    }

    /* compiled from: Unknown */
    public interface zzd<T extends IInterface> {
        T zzW(IBinder iBinder);

        void zza(int i, T t);

        String zzgu();

        String zzgv();
    }

    /* compiled from: Unknown */
    public interface zze<T extends zzd, O> {
        int getPriority();

        int zzoU();

        T zzq(O o);
    }

    /* compiled from: Unknown */
    public static final class zzf<C extends zzd> {
    }

    public <C extends zzb> Api(String name, zza<C, O> clientBuilder, zzc<C> clientKey) {
        zzx.zzb((Object) clientBuilder, (Object) "Cannot construct an Api with a null ClientBuilder");
        zzx.zzb((Object) clientKey, (Object) "Cannot construct an Api with a null ClientKey");
        this.mName = name;
        this.zzafW = clientBuilder;
        this.zzaeE = clientKey;
        this.zzafY = null;
    }

    public String getName() {
        return this.mName;
    }

    public zza<?, O> zzoP() {
        zzx.zza(this.zzafW != null, (Object) "This API was constructed with a SimpleClientBuilder. Use getSimpleClientBuilder");
        return this.zzafW;
    }

    public zze<?, O> zzoQ() {
        zzx.zza(this.zzafX != null, (Object) "This API was constructed with a ClientBuilder. Use getClientBuilder");
        return this.zzafX;
    }

    public zzc<?> zzoR() {
        zzx.zza(this.zzaeE != null, (Object) "This API was constructed with a SimpleClientKey. Use getSimpleClientKey");
        return this.zzaeE;
    }

    public boolean zzoS() {
        return this.zzafY != null;
    }
}
