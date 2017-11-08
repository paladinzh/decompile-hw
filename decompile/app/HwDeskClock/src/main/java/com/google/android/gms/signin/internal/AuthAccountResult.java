package com.google.android.gms.signin.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class AuthAccountResult implements SafeParcelable {
    public static final Creator<AuthAccountResult> CREATOR = new zza();
    final int mVersionCode;

    public AuthAccountResult() {
        this(1);
    }

    AuthAccountResult(int versionCode) {
        this.mVersionCode = versionCode;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zza.zza(this, dest, flags);
    }
}
