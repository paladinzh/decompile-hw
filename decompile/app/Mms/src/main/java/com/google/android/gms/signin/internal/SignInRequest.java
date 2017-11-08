package com.google.android.gms.signin.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.ResolveAccountRequest;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class SignInRequest implements SafeParcelable {
    public static final Creator<SignInRequest> CREATOR = new zzi();
    final int mVersionCode;
    final ResolveAccountRequest zzbhj;

    SignInRequest(int versionCode, ResolveAccountRequest resolveAccountRequest) {
        this.mVersionCode = versionCode;
        this.zzbhj = resolveAccountRequest;
    }

    public SignInRequest(ResolveAccountRequest resolveAccountRequest) {
        this(1, resolveAccountRequest);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzi.zza(this, dest, flags);
    }

    public ResolveAccountRequest zzFO() {
        return this.zzbhj;
    }
}
