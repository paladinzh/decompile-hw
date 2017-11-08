package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class GetCapabilityResponse implements SafeParcelable {
    public static final Creator<GetCapabilityResponse> CREATOR = new zzah();
    public final int statusCode;
    public final int versionCode;
    public final CapabilityInfoParcelable zzbav;

    GetCapabilityResponse(int versionCode, int statusCode, CapabilityInfoParcelable capability) {
        this.versionCode = versionCode;
        this.statusCode = statusCode;
        this.zzbav = capability;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzah.zza(this, dest, flags);
    }
}
