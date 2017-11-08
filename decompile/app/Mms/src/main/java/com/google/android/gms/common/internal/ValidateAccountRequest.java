package com.google.android.gms.common.internal;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

@Deprecated
/* compiled from: Unknown */
public class ValidateAccountRequest implements SafeParcelable {
    public static final Creator<ValidateAccountRequest> CREATOR = new zzae();
    final int mVersionCode;
    private final String zzVO;
    private final Scope[] zzafT;
    final IBinder zzakA;
    private final int zzamy;
    private final Bundle zzamz;

    ValidateAccountRequest(int versionCode, int clientVersion, IBinder accountAccessorBinder, Scope[] scopes, Bundle extraArgs, String callingPackage) {
        this.mVersionCode = versionCode;
        this.zzamy = clientVersion;
        this.zzakA = accountAccessorBinder;
        this.zzafT = scopes;
        this.zzamz = extraArgs;
        this.zzVO = callingPackage;
    }

    public int describeContents() {
        return 0;
    }

    public String getCallingPackage() {
        return this.zzVO;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzae.zza(this, dest, flags);
    }

    public Scope[] zzrd() {
        return this.zzafT;
    }

    public int zzre() {
        return this.zzamy;
    }

    public Bundle zzrf() {
        return this.zzamz;
    }
}
