package com.google.android.gms.signin.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ServerAuthCodeCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.ServerAuthCodeCallbacks.CheckResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.AuthAccountRequest;
import com.google.android.gms.common.internal.BinderWrapper;
import com.google.android.gms.common.internal.ResolveAccountRequest;
import com.google.android.gms.common.internal.ResolveAccountResponse;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.common.internal.zzp;
import com.google.android.gms.common.internal.zzt;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.signin.zzd;
import com.google.android.gms.signin.zze;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/* compiled from: Unknown */
public class zzi extends zzj<zzf> implements zzd {
    private final zzf zzZH;
    private final boolean zzaOp;
    private final ExecutorService zzaOq;
    private final zze zzadf;
    private Integer zzadg;

    /* compiled from: Unknown */
    private static class zza extends com.google.android.gms.signin.internal.zzd.zza {
        private final ExecutorService zzaOq;
        private final zze zzadf;

        public zza(zze zze, ExecutorService executorService) {
            this.zzadf = zze;
            this.zzaOq = executorService;
        }

        private ServerAuthCodeCallbacks zzzs() throws RemoteException {
            return this.zzadf.zzzs();
        }

        public void zza(final String str, final String str2, final zzf zzf) throws RemoteException {
            this.zzaOq.submit(new Runnable(this) {
                final /* synthetic */ zza zzaOu;

                public void run() {
                    try {
                        zzf.zzaq(this.zzaOu.zzzs().onUploadServerAuthCode(str, str2));
                    } catch (Throwable e) {
                        Log.e("SignInClientImpl", "RemoteException thrown when processing uploadServerAuthCode callback", e);
                    }
                }
            });
        }

        public void zza(final String str, final List<Scope> list, final zzf zzf) throws RemoteException {
            this.zzaOq.submit(new Runnable(this) {
                final /* synthetic */ zza zzaOu;

                public void run() {
                    try {
                        CheckResult onCheckServerAuthorization = this.zzaOu.zzzs().onCheckServerAuthorization(str, Collections.unmodifiableSet(new HashSet(list)));
                        zzf.zza(new CheckServerAuthResult(onCheckServerAuthorization.zznm(), onCheckServerAuthorization.zznn()));
                    } catch (Throwable e) {
                        Log.e("SignInClientImpl", "RemoteException thrown when processing checkServerAuthorization callback", e);
                    }
                }
            });
        }
    }

    public zzi(Context context, Looper looper, boolean z, zzf zzf, zze zze, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, ExecutorService executorService) {
        super(context, looper, 44, zzf, connectionCallbacks, onConnectionFailedListener);
        this.zzaOp = z;
        this.zzZH = zzf;
        this.zzadf = zzf.zzop();
        this.zzadg = zzf.zzoq();
        this.zzaOq = executorService;
    }

    public static Bundle zza(zze zze, Integer num, ExecutorService executorService) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("com.google.android.gms.signin.internal.offlineAccessRequested", zze.zzzq());
        bundle.putBoolean("com.google.android.gms.signin.internal.idTokenRequested", zze.zzzr());
        bundle.putString("com.google.android.gms.signin.internal.serverClientId", zze.zzlG());
        if (zze.zzzs() != null) {
            bundle.putParcelable("com.google.android.gms.signin.internal.signInCallbacks", new BinderWrapper(new zza(zze, executorService).asBinder()));
        }
        if (num != null) {
            bundle.putInt("com.google.android.gms.common.internal.ClientSettings.sessionId", num.intValue());
        }
        return bundle;
    }

    public void connect() {
        zza(new zzf(this));
    }

    protected /* synthetic */ IInterface zzV(IBinder iBinder) {
        return zzdI(iBinder);
    }

    public void zza(zzp zzp, Set<Scope> set, zze zze) {
        zzx.zzb((Object) zze, (Object) "Expecting a valid ISignInCallbacks");
        try {
            ((zzf) zzoC()).zza(new AuthAccountRequest(zzp, set), zze);
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when authAccount is called");
            try {
                zze.zza(new ConnectionResult(8, null), new AuthAccountResult());
            } catch (RemoteException e2) {
                Log.wtf("SignInClientImpl", "ISignInCallbacks#onAuthAccount should be executed from the same process, unexpected RemoteException.");
            }
        }
    }

    public void zza(zzp zzp, boolean z) {
        try {
            ((zzf) zzoC()).zza(zzp, this.zzadg.intValue(), z);
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when saveDefaultAccount is called");
        }
    }

    public void zza(zzt zzt) {
        zzx.zzb((Object) zzt, (Object) "Expecting a valid IResolveAccountCallbacks");
        try {
            ((zzf) zzoC()).zza(new ResolveAccountRequest(this.zzZH.zzoh(), this.zzadg.intValue()), zzt);
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when resolveAccount is called");
            try {
                zzt.zzb(new ResolveAccountResponse(8));
            } catch (RemoteException e2) {
                Log.wtf("SignInClientImpl", "IResolveAccountCallbacks#onAccountResolutionComplete should be executed from the same process, unexpected RemoteException.");
            }
        }
    }

    protected zzf zzdI(IBinder iBinder) {
        return com.google.android.gms.signin.internal.zzf.zza.zzdH(iBinder);
    }

    protected String zzfA() {
        return "com.google.android.gms.signin.service.START";
    }

    protected String zzfB() {
        return "com.google.android.gms.signin.internal.ISignInService";
    }

    protected Bundle zzli() {
        Bundle zza = zza(this.zzadf, this.zzZH.zzoq(), this.zzaOq);
        if (!getContext().getPackageName().equals(this.zzZH.zzom())) {
            zza.putString("com.google.android.gms.signin.internal.realClientPackageName", this.zzZH.zzom());
        }
        return zza;
    }

    public boolean zzlm() {
        return this.zzaOp;
    }

    public void zzzp() {
        try {
            ((zzf) zzoC()).zzja(this.zzadg.intValue());
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when clearAccountFromSessionStore is called");
        }
    }
}
