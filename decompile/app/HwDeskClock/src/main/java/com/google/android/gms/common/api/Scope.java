package com.google.android.gms.common.api;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public final class Scope implements SafeParcelable {
    public static final Creator<Scope> CREATOR = new zzm();
    final int mVersionCode;
    private final String zzaaD;

    Scope(int versionCode, String scopeUri) {
        zzx.zzh(scopeUri, "scopeUri must not be null or empty");
        this.mVersionCode = versionCode;
        this.zzaaD = scopeUri;
    }

    public Scope(String scopeUri) {
        this(1, scopeUri);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        return this != o ? o instanceof Scope ? this.zzaaD.equals(((Scope) o).zzaaD) : false : true;
    }

    public int hashCode() {
        return this.zzaaD.hashCode();
    }

    public String toString() {
        return this.zzaaD;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzm.zza(this, dest, flags);
    }

    public String zznH() {
        return this.zzaaD;
    }
}
