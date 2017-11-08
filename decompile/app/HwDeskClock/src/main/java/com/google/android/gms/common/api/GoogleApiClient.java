package com.google.android.gms.common.api;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Api.ApiOptions;
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzld;
import com.google.android.gms.signin.zzb;
import com.google.android.gms.signin.zzd;
import com.google.android.gms.signin.zze;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
public interface GoogleApiClient {

    /* compiled from: Unknown */
    public interface ConnectionCallbacks {
        void onConnected(Bundle bundle);

        void onConnectionSuspended(int i);
    }

    /* compiled from: Unknown */
    public interface OnConnectionFailedListener {
        void onConnectionFailed(ConnectionResult connectionResult);
    }

    /* compiled from: Unknown */
    public static final class Builder {
        private final Context mContext;
        private Account zzOY;
        private String zzQl;
        private Looper zzYV;
        private final Set<Scope> zzYY = new HashSet();
        private int zzYZ;
        private View zzZa;
        private String zzZb;
        private final Map<Api<?>, com.google.android.gms.common.internal.zzf.zza> zzZc = new zzld();
        private final Map<Api<?>, ApiOptions> zzZd = new zzld();
        private FragmentActivity zzZe;
        private int zzZf = -1;
        private int zzZg = -1;
        private OnConnectionFailedListener zzZh;
        private GoogleApiAvailability zzZi = GoogleApiAvailability.getInstance();
        private com.google.android.gms.common.api.Api.zza<? extends zzd, zze> zzZj = zzb.zzQg;
        private final ArrayList<ConnectionCallbacks> zzZk = new ArrayList();
        private final ArrayList<OnConnectionFailedListener> zzZl = new ArrayList();
        private com.google.android.gms.signin.zze.zza zzZm = new com.google.android.gms.signin.zze.zza();

        public Builder(Context context) {
            this.mContext = context;
            this.zzYV = context.getMainLooper();
            this.zzQl = context.getPackageName();
            this.zzZb = context.getClass().getName();
        }

        private void zza(zzp zzp, GoogleApiClient googleApiClient) {
            zzp.zza(this.zzZf, googleApiClient, this.zzZh);
        }

        private GoogleApiClient zznk() {
            final GoogleApiClient zzi = new zzi(this.mContext.getApplicationContext(), this.zzYV, zznj(), this.zzZi, this.zzZj, this.zzZd, this.zzZk, this.zzZl, this.zzZf, -1);
            zzp zza = zzp.zza(this.zzZe);
            if (zza != null) {
                zza(zza, zzi);
            } else {
                new Handler(this.mContext.getMainLooper()).post(new Runnable(this) {
                    final /* synthetic */ Builder zzZn;

                    public void run() {
                        if (!this.zzZn.zzZe.isFinishing() && !this.zzZn.zzZe.getSupportFragmentManager().isDestroyed()) {
                            this.zzZn.zza(zzp.zzb(this.zzZn.zzZe), zzi);
                        }
                    }
                });
            }
            return zzi;
        }

        private GoogleApiClient zznl() {
            zzq zzc = zzq.zzc(this.zzZe);
            GoogleApiClient zzbj = zzc.zzbj(this.zzZg);
            if (zzbj == null) {
                zzbj = new zzi(this.mContext.getApplicationContext(), this.zzYV, zznj(), this.zzZi, this.zzZj, this.zzZd, this.zzZk, this.zzZl, -1, this.zzZg);
            }
            zzc.zza(this.zzZg, zzbj, this.zzZh);
            return zzbj;
        }

        public Builder addApi(Api<? extends NotRequiredOptions> api) {
            this.zzZd.put(api, null);
            this.zzYY.addAll(api.zznb().zzl(null));
            return this;
        }

        public Builder addConnectionCallbacks(ConnectionCallbacks listener) {
            this.zzZk.add(listener);
            return this;
        }

        public Builder addOnConnectionFailedListener(OnConnectionFailedListener listener) {
            this.zzZl.add(listener);
            return this;
        }

        public GoogleApiClient build() {
            boolean z = false;
            if (!this.zzZd.isEmpty()) {
                z = true;
            }
            zzx.zzb(z, (Object) "must call addApi() to add at least one API");
            return this.zzZf < 0 ? this.zzZg < 0 ? new zzi(this.mContext, this.zzYV, zznj(), this.zzZi, this.zzZj, this.zzZd, this.zzZk, this.zzZl, -1, -1) : zznl() : zznk();
        }

        public zzf zznj() {
            return new zzf(this.zzOY, this.zzYY, this.zzZc, this.zzYZ, this.zzZa, this.zzQl, this.zzZb, this.zzZm.zzzt());
        }
    }

    /* compiled from: Unknown */
    public interface ServerAuthCodeCallbacks {

        /* compiled from: Unknown */
        public static class CheckResult {
            private boolean zzZo;
            private Set<Scope> zzZp;

            public boolean zznm() {
                return this.zzZo;
            }

            public Set<Scope> zznn() {
                return this.zzZp;
            }
        }

        CheckResult onCheckServerAuthorization(String str, Set<Scope> set);

        boolean onUploadServerAuthCode(String str, String str2);
    }

    /* compiled from: Unknown */
    public interface zza {
        void zza(ConnectionResult connectionResult);

        void zzb(ConnectionResult connectionResult);
    }

    ConnectionResult blockingConnect(long j, TimeUnit timeUnit);

    void connect();

    void disconnect();

    void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    Looper getLooper();

    boolean isConnected();

    boolean isConnecting();

    void registerConnectionCallbacks(ConnectionCallbacks connectionCallbacks);

    void registerConnectionFailedListener(OnConnectionFailedListener onConnectionFailedListener);

    void unregisterConnectionCallbacks(ConnectionCallbacks connectionCallbacks);

    void unregisterConnectionFailedListener(OnConnectionFailedListener onConnectionFailedListener);

    <A extends Api.zzb, R extends Result, T extends com.google.android.gms.common.api.zzc.zza<R, A>> T zza(T t);
}
