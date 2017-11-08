package com.google.android.gms.auth.api.credentials.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class DeleteRequest implements SafeParcelable {
    public static final Creator<DeleteRequest> CREATOR = new zzf();
    final int mVersionCode;
    private final Credential zzRx;

    DeleteRequest(int version, Credential credential) {
        this.mVersionCode = version;
        this.zzRx = credential;
    }

    public int describeContents() {
        return 0;
    }

    public Credential getCredential() {
        return this.zzRx;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzf.zza(this, out, flags);
    }
}
