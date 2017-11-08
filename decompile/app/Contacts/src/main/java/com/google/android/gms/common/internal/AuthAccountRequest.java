package com.google.android.gms.common.internal;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class AuthAccountRequest implements SafeParcelable {
    public static final Creator<AuthAccountRequest> CREATOR = new zzc();
    final int mVersionCode;
    final Scope[] zzafT;
    final IBinder zzakA;
    Integer zzakB;
    Integer zzakC;

    AuthAccountRequest(int versionCode, IBinder accountAccessorBinder, Scope[] scopes, Integer oauthPolicy, Integer policyAction) {
        this.mVersionCode = versionCode;
        this.zzakA = accountAccessorBinder;
        this.zzafT = scopes;
        this.zzakB = oauthPolicy;
        this.zzakC = policyAction;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzc.zza(this, dest, flags);
    }
}
