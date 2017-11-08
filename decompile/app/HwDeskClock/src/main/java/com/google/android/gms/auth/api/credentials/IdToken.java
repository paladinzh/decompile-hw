package com.google.android.gms.auth.api.credentials;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class IdToken implements SafeParcelable {
    public static final Creator<IdToken> CREATOR = new zzd();
    final int mVersionCode;
    private final String zzRf;
    private final String zzRn;

    IdToken(int version, String accountType, String idToken) {
        this.mVersionCode = version;
        this.zzRf = accountType;
        this.zzRn = idToken;
    }

    public int describeContents() {
        return 0;
    }

    public String getAccountType() {
        return this.zzRf;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzd.zza(this, out, flags);
    }

    public String zzlv() {
        return this.zzRn;
    }
}
