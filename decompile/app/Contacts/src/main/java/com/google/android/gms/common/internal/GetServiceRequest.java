package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzp.zza;
import com.google.android.gms.common.zzc;
import java.util.Collection;

/* compiled from: Unknown */
public class GetServiceRequest implements SafeParcelable {
    public static final Creator<GetServiceRequest> CREATOR = new zzi();
    final int version;
    final int zzall;
    int zzalm;
    String zzaln;
    IBinder zzalo;
    Scope[] zzalp;
    Bundle zzalq;
    Account zzalr;

    public GetServiceRequest(int serviceId) {
        this.version = 2;
        this.zzalm = zzc.GOOGLE_PLAY_SERVICES_VERSION_CODE;
        this.zzall = serviceId;
    }

    GetServiceRequest(int version, int serviceId, int clientVersion, String callingPackage, IBinder accountAccessorBinder, Scope[] scopes, Bundle extraArgs, Account clientRequestedAccount) {
        this.version = version;
        this.zzall = serviceId;
        this.zzalm = clientVersion;
        this.zzaln = callingPackage;
        if (version >= 2) {
            this.zzalo = accountAccessorBinder;
            this.zzalr = clientRequestedAccount;
        } else {
            this.zzalr = zzaO(accountAccessorBinder);
        }
        this.zzalp = scopes;
        this.zzalq = extraArgs;
    }

    private Account zzaO(IBinder iBinder) {
        return iBinder == null ? null : zza.zza(zza.zzaP(iBinder));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzi.zza(this, dest, flags);
    }

    public GetServiceRequest zzb(zzp zzp) {
        if (zzp != null) {
            this.zzalo = zzp.asBinder();
        }
        return this;
    }

    public GetServiceRequest zzc(Account account) {
        this.zzalr = account;
        return this;
    }

    public GetServiceRequest zzcG(String str) {
        this.zzaln = str;
        return this;
    }

    public GetServiceRequest zzd(Collection<Scope> collection) {
        this.zzalp = (Scope[]) collection.toArray(new Scope[collection.size()]);
        return this;
    }

    public GetServiceRequest zzj(Bundle bundle) {
        this.zzalq = bundle;
        return this;
    }
}
