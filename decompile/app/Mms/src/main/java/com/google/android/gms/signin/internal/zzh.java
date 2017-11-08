package com.google.android.gms.signin.internal;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.internal.zzq;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.ResolveAccountRequest;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.common.internal.zzp;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzrn;
import com.google.android.gms.internal.zzro;
import com.google.android.gms.signin.internal.zze.zza;

/* compiled from: Unknown */
public class zzh extends zzj<zze> implements zzrn {
    private final zzf zzahz;
    private Integer zzale;
    private final Bundle zzbgU;
    private final boolean zzbhi;

    public zzh(Context context, Looper looper, boolean z, zzf zzf, Bundle bundle, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 44, zzf, connectionCallbacks, onConnectionFailedListener);
        this.zzbhi = z;
        this.zzahz = zzf;
        this.zzbgU = bundle;
        this.zzale = zzf.zzqz();
    }

    public zzh(Context context, Looper looper, boolean z, zzf zzf, zzro zzro, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        this(context, looper, z, zzf, zza(zzf), connectionCallbacks, onConnectionFailedListener);
    }

    private ResolveAccountRequest zzFN() {
        Account zzqq = this.zzahz.zzqq();
        GoogleSignInAccount googleSignInAccount = null;
        if ("<<default account>>".equals(zzqq.name)) {
            googleSignInAccount = zzq.zzaf(getContext()).zzno();
        }
        return new ResolveAccountRequest(zzqq, this.zzale.intValue(), googleSignInAccount);
    }

    public static Bundle zza(zzf zzf) {
        zzro zzqy = zzf.zzqy();
        Integer zzqz = zzf.zzqz();
        Bundle bundle = new Bundle();
        bundle.putParcelable("com.google.android.gms.signin.internal.clientRequestedAccount", zzf.getAccount());
        if (zzqz != null) {
            bundle.putInt("com.google.android.gms.common.internal.ClientSettings.sessionId", zzqz.intValue());
        }
        if (zzqy != null) {
            bundle.putBoolean("com.google.android.gms.signin.internal.offlineAccessRequested", zzqy.zzFH());
            bundle.putBoolean("com.google.android.gms.signin.internal.idTokenRequested", zzqy.zzmO());
            bundle.putString("com.google.android.gms.signin.internal.serverClientId", zzqy.zzmR());
            bundle.putBoolean("com.google.android.gms.signin.internal.usePromptModeForAuthCode", true);
            bundle.putBoolean("com.google.android.gms.signin.internal.forceCodeForRefreshToken", zzqy.zzmQ());
            bundle.putString("com.google.android.gms.signin.internal.hostedDomain", zzqy.zzmS());
            bundle.putBoolean("com.google.android.gms.signin.internal.waitForAccessTokenRefresh", zzqy.zzFI());
        }
        return bundle;
    }

    public void connect() {
        zza(new zzf(this));
    }

    public void zzFG() {
        try {
            ((zze) zzqJ()).zzka(this.zzale.intValue());
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when clearAccountFromSessionStore is called");
        }
    }

    protected /* synthetic */ IInterface zzW(IBinder iBinder) {
        return zzec(iBinder);
    }

    public void zza(zzp zzp, boolean z) {
        try {
            ((zze) zzqJ()).zza(zzp, this.zzale.intValue(), z);
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when saveDefaultAccount is called");
        }
    }

    public void zza(zzd zzd) {
        zzx.zzb((Object) zzd, (Object) "Expecting a valid ISignInCallbacks");
        try {
            ((zze) zzqJ()).zza(new SignInRequest(zzFN()), zzd);
        } catch (Throwable e) {
            Log.w("SignInClientImpl", "Remote service probably died when signIn is called");
            try {
                zzd.zzb(new SignInResponse(8));
            } catch (RemoteException e2) {
                Log.wtf("SignInClientImpl", "ISignInCallbacks#onSignInComplete should be executed from the same process, unexpected RemoteException.", e);
            }
        }
    }

    protected zze zzec(IBinder iBinder) {
        return zza.zzeb(iBinder);
    }

    protected String zzgu() {
        return "com.google.android.gms.signin.service.START";
    }

    protected String zzgv() {
        return "com.google.android.gms.signin.internal.ISignInService";
    }

    public boolean zzmE() {
        return this.zzbhi;
    }

    protected Bundle zzml() {
        if (!getContext().getPackageName().equals(this.zzahz.zzqv())) {
            this.zzbgU.putString("com.google.android.gms.signin.internal.realClientPackageName", this.zzahz.zzqv());
        }
        return this.zzbgU;
    }
}
