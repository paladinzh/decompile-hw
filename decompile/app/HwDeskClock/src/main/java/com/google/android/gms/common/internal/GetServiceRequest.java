package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzp.zza;
import java.util.Collection;

/* compiled from: Unknown */
public class GetServiceRequest implements SafeParcelable {
    public static final Creator<GetServiceRequest> CREATOR = new zzi();
    final int version;
    final int zzado;
    int zzadp;
    String zzadq;
    IBinder zzadr;
    Scope[] zzads;
    Bundle zzadt;
    Account zzadu;

    public GetServiceRequest(int serviceId) {
        this.version = 2;
        this.zzadp = GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE;
        this.zzado = serviceId;
    }

    GetServiceRequest(int version, int serviceId, int clientVersion, String callingPackage, IBinder accountAccessorBinder, Scope[] scopes, Bundle extraArgs, Account clientRequestedAccount) {
        this.version = version;
        this.zzado = serviceId;
        this.zzadp = clientVersion;
        this.zzadq = callingPackage;
        if (version >= 2) {
            this.zzadr = accountAccessorBinder;
            this.zzadu = clientRequestedAccount;
        } else {
            this.zzadu = zzaG(accountAccessorBinder);
        }
        this.zzads = scopes;
        this.zzadt = extraArgs;
    }

    private Account zzaG(IBinder iBinder) {
        return iBinder == null ? null : zza.zzb(zza.zzaH(iBinder));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzi.zza(this, dest, flags);
    }

    public GetServiceRequest zzb(Account account) {
        this.zzadu = account;
        return this;
    }

    public GetServiceRequest zzc(zzp zzp) {
        if (zzp != null) {
            this.zzadr = zzp.asBinder();
        }
        return this;
    }

    public GetServiceRequest zzck(String str) {
        this.zzadq = str;
        return this;
    }

    public GetServiceRequest zzd(Collection<Scope> collection) {
        this.zzads = (Scope[]) collection.toArray(new Scope[collection.size()]);
        return this;
    }

    public GetServiceRequest zzg(Bundle bundle) {
        this.zzadt = bundle;
        return this;
    }
}
