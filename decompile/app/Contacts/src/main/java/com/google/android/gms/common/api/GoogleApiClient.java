package com.google.android.gms.common.api;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api.ApiOptions;
import com.google.android.gms.common.api.Api.ApiOptions.HasOptions;
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.Api.zzd;
import com.google.android.gms.common.api.Api.zze;
import com.google.android.gms.common.api.internal.zzj;
import com.google.android.gms.common.api.internal.zzq;
import com.google.android.gms.common.api.internal.zzu;
import com.google.android.gms.common.api.internal.zzw;
import com.google.android.gms.common.internal.zzad;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.zzc;
import com.google.android.gms.internal.zzrl;
import com.google.android.gms.internal.zzrn;
import com.google.android.gms.internal.zzro;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/* compiled from: Unknown */
public abstract class GoogleApiClient {
    public static final int SIGN_IN_MODE_OPTIONAL = 2;
    public static final int SIGN_IN_MODE_REQUIRED = 1;
    private static final Set<GoogleApiClient> zzagg = Collections.newSetFromMap(new WeakHashMap());

    /* compiled from: Unknown */
    public interface ConnectionCallbacks {
        public static final int CAUSE_NETWORK_LOST = 2;
        public static final int CAUSE_SERVICE_DISCONNECTED = 1;

        void onConnected(@Nullable Bundle bundle);

        void onConnectionSuspended(int i);
    }

    /* compiled from: Unknown */
    public interface OnConnectionFailedListener {
        void onConnectionFailed(@NonNull ConnectionResult connectionResult);
    }

    /* compiled from: Unknown */
    public static final class Builder {
        private final Context mContext;
        private Account zzTI;
        private String zzUW;
        private final Set<Scope> zzagh;
        private final Set<Scope> zzagi;
        private int zzagj;
        private View zzagk;
        private String zzagl;
        private final Map<Api<?>, com.google.android.gms.common.internal.zzf.zza> zzagm;
        private final Map<Api<?>, ApiOptions> zzagn;
        private FragmentActivity zzago;
        private int zzagp;
        private OnConnectionFailedListener zzagq;
        private Looper zzagr;
        private zzc zzags;
        private com.google.android.gms.common.api.Api.zza<? extends zzrn, zzro> zzagt;
        private final ArrayList<ConnectionCallbacks> zzagu;
        private final ArrayList<OnConnectionFailedListener> zzagv;

        public Builder(@NonNull Context context) {
            this.zzagh = new HashSet();
            this.zzagi = new HashSet();
            this.zzagm = new ArrayMap();
            this.zzagn = new ArrayMap();
            this.zzagp = -1;
            this.zzags = zzc.zzoK();
            this.zzagt = zzrl.zzUJ;
            this.zzagu = new ArrayList();
            this.zzagv = new ArrayList();
            this.mContext = context;
            this.zzagr = context.getMainLooper();
            this.zzUW = context.getPackageName();
            this.zzagl = context.getClass().getName();
        }

        public Builder(@NonNull Context context, @NonNull ConnectionCallbacks connectedListener, @NonNull OnConnectionFailedListener connectionFailedListener) {
            this(context);
            zzx.zzb((Object) connectedListener, (Object) "Must provide a connected listener");
            this.zzagu.add(connectedListener);
            zzx.zzb((Object) connectionFailedListener, (Object) "Must provide a connection failed listener");
            this.zzagv.add(connectionFailedListener);
        }

        private static <C extends zzb, O> C zza(com.google.android.gms.common.api.Api.zza<C, O> zza, Object obj, Context context, Looper looper, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zza.zza(context, looper, zzf, obj, connectionCallbacks, onConnectionFailedListener);
        }

        private static <C extends zzd, O> zzad zza(zze<C, O> zze, Object obj, Context context, Looper looper, zzf zzf, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzad(context, looper, zze.zzoU(), connectionCallbacks, onConnectionFailedListener, zzf, zze.zzq(obj));
        }

        private <O extends ApiOptions> void zza(Api<O> api, O o, int i, Scope... scopeArr) {
            boolean z = true;
            int i2 = 0;
            if (i != 1) {
                if (i != 2) {
                    throw new IllegalArgumentException("Invalid resolution mode: '" + i + "', use a constant from GoogleApiClient.ResolutionMode");
                }
                z = false;
            }
            Set hashSet = new HashSet(api.zzoP().zzo(o));
            int length = scopeArr.length;
            while (i2 < length) {
                hashSet.add(scopeArr[i2]);
                i2++;
            }
            this.zzagm.put(api, new com.google.android.gms.common.internal.zzf.zza(hashSet, z));
        }

        private void zza(zzw zzw, GoogleApiClient googleApiClient) {
            zzw.zza(this.zzagp, googleApiClient, this.zzagq);
        }

        private void zze(final GoogleApiClient googleApiClient) {
            zzw zza = zzw.zza(this.zzago);
            if (zza != null) {
                zza(zza, googleApiClient);
            } else {
                new Handler(this.mContext.getMainLooper()).post(new Runnable(this) {
                    final /* synthetic */ Builder zzagw;

                    public void run() {
                        if (!this.zzagw.zzago.isFinishing() && !this.zzagw.zzago.getSupportFragmentManager().isDestroyed()) {
                            this.zzagw.zza(zzw.zzb(this.zzagw.zzago), googleApiClient);
                        }
                    }
                });
            }
        }

        private GoogleApiClient zzoZ() {
            zzf zzoY = zzoY();
            Map zzqu = zzoY.zzqu();
            Map arrayMap = new ArrayMap();
            Map arrayMap2 = new ArrayMap();
            ArrayList arrayList = new ArrayList();
            Api api = null;
            Api api2 = null;
            for (Api api3 : this.zzagn.keySet()) {
                Api api4;
                zzb zza;
                Api api5;
                Object obj = this.zzagn.get(api3);
                int i = 0;
                if (zzqu.get(api3) != null) {
                    i = !((com.google.android.gms.common.internal.zzf.zza) zzqu.get(api3)).zzalf ? 2 : 1;
                }
                arrayMap.put(api3, Integer.valueOf(i));
                ConnectionCallbacks zzc = new com.google.android.gms.common.api.internal.zzc(api3, i);
                arrayList.add(zzc);
                if (api3.zzoS()) {
                    zze zzoQ = api3.zzoQ();
                    api4 = zzoQ.getPriority() != 1 ? api : api3;
                    zza = zza(zzoQ, obj, this.mContext, this.zzagr, zzoY, zzc, (OnConnectionFailedListener) zzc);
                } else {
                    com.google.android.gms.common.api.Api.zza zzoP = api3.zzoP();
                    api4 = zzoP.getPriority() != 1 ? api : api3;
                    zza = zza(zzoP, obj, this.mContext, this.zzagr, zzoY, zzc, (OnConnectionFailedListener) zzc);
                }
                zzb zzb = zza;
                Api api6 = api4;
                arrayMap2.put(api3.zzoR(), zzb);
                if (!zzb.zznb()) {
                    api5 = api2;
                } else if (api2 == null) {
                    api5 = api3;
                } else {
                    throw new IllegalStateException(api3.getName() + " cannot be used with " + api2.getName());
                }
                api = api6;
                api2 = api5;
            }
            if (api2 != null) {
                if (api == null) {
                    zzx.zza(this.zzTI == null, "Must not set an account in GoogleApiClient.Builder when using %s. Set account in GoogleSignInOptions.Builder instead", api2.getName());
                    zzx.zza(this.zzagh.equals(this.zzagi), "Must not set scopes in GoogleApiClient.Builder when using %s. Set account in GoogleSignInOptions.Builder instead.", api2.getName());
                } else {
                    throw new IllegalStateException(api2.getName() + " cannot be used with " + api.getName());
                }
            }
            return new zzj(this.mContext, new ReentrantLock(), this.zzagr, zzoY, this.zzags, this.zzagt, arrayMap, this.zzagu, this.zzagv, arrayMap2, this.zzagp, zzj.zza(arrayMap2.values(), true), arrayList);
        }

        public Builder addApi(@NonNull Api<? extends NotRequiredOptions> api) {
            zzx.zzb((Object) api, (Object) "Api must not be null");
            this.zzagn.put(api, null);
            Collection zzo = api.zzoP().zzo(null);
            this.zzagi.addAll(zzo);
            this.zzagh.addAll(zzo);
            return this;
        }

        public <O extends HasOptions> Builder addApi(@NonNull Api<O> api, @NonNull O options) {
            zzx.zzb((Object) api, (Object) "Api must not be null");
            zzx.zzb((Object) options, (Object) "Null options are not permitted for this Api");
            this.zzagn.put(api, options);
            Collection zzo = api.zzoP().zzo(options);
            this.zzagi.addAll(zzo);
            this.zzagh.addAll(zzo);
            return this;
        }

        public <O extends HasOptions> Builder addApiIfAvailable(@NonNull Api<O> api, @NonNull O options, Scope... scopes) {
            zzx.zzb((Object) api, (Object) "Api must not be null");
            zzx.zzb((Object) options, (Object) "Null options are not permitted for this Api");
            this.zzagn.put(api, options);
            zza(api, options, 1, scopes);
            return this;
        }

        public Builder addApiIfAvailable(@NonNull Api<? extends NotRequiredOptions> api, Scope... scopes) {
            zzx.zzb((Object) api, (Object) "Api must not be null");
            this.zzagn.put(api, null);
            zza(api, null, 1, scopes);
            return this;
        }

        public Builder addConnectionCallbacks(@NonNull ConnectionCallbacks listener) {
            zzx.zzb((Object) listener, (Object) "Listener must not be null");
            this.zzagu.add(listener);
            return this;
        }

        public Builder addOnConnectionFailedListener(@NonNull OnConnectionFailedListener listener) {
            zzx.zzb((Object) listener, (Object) "Listener must not be null");
            this.zzagv.add(listener);
            return this;
        }

        public Builder addScope(@NonNull Scope scope) {
            zzx.zzb((Object) scope, (Object) "Scope must not be null");
            this.zzagh.add(scope);
            return this;
        }

        public GoogleApiClient build() {
            boolean z = false;
            if (!this.zzagn.isEmpty()) {
                z = true;
            }
            zzx.zzb(z, (Object) "must call addApi() to add at least one API");
            GoogleApiClient zzoZ = zzoZ();
            synchronized (GoogleApiClient.zzagg) {
                GoogleApiClient.zzagg.add(zzoZ);
            }
            if (this.zzagp >= 0) {
                zze(zzoZ);
            }
            return zzoZ;
        }

        public Builder enableAutoManage(@NonNull FragmentActivity fragmentActivity, int clientId, @Nullable OnConnectionFailedListener unresolvedConnectionFailedListener) {
            boolean z = false;
            if (clientId >= 0) {
                z = true;
            }
            zzx.zzb(z, (Object) "clientId must be non-negative");
            this.zzagp = clientId;
            this.zzago = (FragmentActivity) zzx.zzb((Object) fragmentActivity, (Object) "Null activity is not permitted.");
            this.zzagq = unresolvedConnectionFailedListener;
            return this;
        }

        public Builder enableAutoManage(@NonNull FragmentActivity fragmentActivity, @Nullable OnConnectionFailedListener unresolvedConnectionFailedListener) {
            return enableAutoManage(fragmentActivity, 0, unresolvedConnectionFailedListener);
        }

        public Builder setAccountName(String accountName) {
            Account account = null;
            if (accountName != null) {
                account = new Account(accountName, "com.google");
            }
            this.zzTI = account;
            return this;
        }

        public Builder setGravityForPopups(int gravityForPopups) {
            this.zzagj = gravityForPopups;
            return this;
        }

        public Builder setHandler(@NonNull Handler handler) {
            zzx.zzb((Object) handler, (Object) "Handler must not be null");
            this.zzagr = handler.getLooper();
            return this;
        }

        public Builder setViewForPopups(@NonNull View viewForPopups) {
            zzx.zzb((Object) viewForPopups, (Object) "View must not be null");
            this.zzagk = viewForPopups;
            return this;
        }

        public Builder useDefaultAccount() {
            return setAccountName("<<default account>>");
        }

        public zzf zzoY() {
            zzro zzro = zzro.zzbgV;
            if (this.zzagn.containsKey(zzrl.API)) {
                zzro = (zzro) this.zzagn.get(zzrl.API);
            }
            return new zzf(this.zzTI, this.zzagh, this.zzagm, this.zzagj, this.zzagk, this.zzUW, this.zzagl, zzro);
        }
    }

    /* compiled from: Unknown */
    public interface zza {
        void zza(@NonNull ConnectionResult connectionResult);
    }

    public static void dumpAll(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        synchronized (zzagg) {
            String str = prefix + "  ";
            int i = 0;
            for (GoogleApiClient googleApiClient : zzagg) {
                int i2 = i + 1;
                writer.append(prefix).append("GoogleApiClient#").println(i);
                googleApiClient.dump(str, fd, writer, args);
                i = i2;
            }
        }
    }

    public static Set<GoogleApiClient> zzoV() {
        return zzagg;
    }

    public abstract ConnectionResult blockingConnect();

    public abstract ConnectionResult blockingConnect(long j, @NonNull TimeUnit timeUnit);

    public abstract PendingResult<Status> clearDefaultAccountAndReconnect();

    public abstract void connect();

    public void connect(int signInMode) {
        throw new UnsupportedOperationException();
    }

    public abstract void disconnect();

    public abstract void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    @NonNull
    public abstract ConnectionResult getConnectionResult(@NonNull Api<?> api);

    public Context getContext() {
        throw new UnsupportedOperationException();
    }

    public Looper getLooper() {
        throw new UnsupportedOperationException();
    }

    public abstract boolean hasConnectedApi(@NonNull Api<?> api);

    public abstract boolean isConnected();

    public abstract boolean isConnecting();

    public abstract boolean isConnectionCallbacksRegistered(@NonNull ConnectionCallbacks connectionCallbacks);

    public abstract boolean isConnectionFailedListenerRegistered(@NonNull OnConnectionFailedListener onConnectionFailedListener);

    public abstract void reconnect();

    public abstract void registerConnectionCallbacks(@NonNull ConnectionCallbacks connectionCallbacks);

    public abstract void registerConnectionFailedListener(@NonNull OnConnectionFailedListener onConnectionFailedListener);

    public abstract void stopAutoManage(@NonNull FragmentActivity fragmentActivity);

    public abstract void unregisterConnectionCallbacks(@NonNull ConnectionCallbacks connectionCallbacks);

    public abstract void unregisterConnectionFailedListener(@NonNull OnConnectionFailedListener onConnectionFailedListener);

    @NonNull
    public <C extends zzb> C zza(@NonNull Api.zzc<C> zzc) {
        throw new UnsupportedOperationException();
    }

    public <A extends zzb, R extends Result, T extends com.google.android.gms.common.api.internal.zza.zza<R, A>> T zza(@NonNull T t) {
        throw new UnsupportedOperationException();
    }

    public void zza(com.google.android.gms.common.api.internal.zzx zzx) {
        throw new UnsupportedOperationException();
    }

    public boolean zza(@NonNull Api<?> api) {
        throw new UnsupportedOperationException();
    }

    public boolean zza(zzu zzu) {
        throw new UnsupportedOperationException();
    }

    public <A extends zzb, T extends com.google.android.gms.common.api.internal.zza.zza<? extends Result, A>> T zzb(@NonNull T t) {
        throw new UnsupportedOperationException();
    }

    public void zzb(com.google.android.gms.common.api.internal.zzx zzx) {
        throw new UnsupportedOperationException();
    }

    public void zzoW() {
        throw new UnsupportedOperationException();
    }

    public <L> zzq<L> zzr(@NonNull L l) {
        throw new UnsupportedOperationException();
    }
}
