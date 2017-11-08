package com.google.android.gms.signin.internal;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class AuthAccountResult implements Result, SafeParcelable {
    public static final Creator<AuthAccountResult> CREATOR = new zza();
    final int mVersionCode;
    private int zzbhd;
    private Intent zzbhe;

    public AuthAccountResult() {
        this(0, null);
    }

    AuthAccountResult(int versionCode, int connectionResultCode, Intent rawAuthResultionIntent) {
        this.mVersionCode = versionCode;
        this.zzbhd = connectionResultCode;
        this.zzbhe = rawAuthResultionIntent;
    }

    public AuthAccountResult(int connectionResultCode, Intent rawAuthResolutionIntent) {
        this(2, connectionResultCode, rawAuthResolutionIntent);
    }

    public int describeContents() {
        return 0;
    }

    public Status getStatus() {
        return this.zzbhd != 0 ? Status.zzagG : Status.zzagC;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zza.zza(this, dest, flags);
    }

    public int zzFK() {
        return this.zzbhd;
    }

    public Intent zzFL() {
        return this.zzbhe;
    }
}
