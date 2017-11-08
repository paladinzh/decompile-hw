package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class ChannelReceiveFileResponse implements SafeParcelable {
    public static final Creator<ChannelReceiveFileResponse> CREATOR = new zzq();
    public final int statusCode;
    public final int versionCode;

    ChannelReceiveFileResponse(int versionCode, int statusCode) {
        this.versionCode = versionCode;
        this.statusCode = statusCode;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzq.zza(this, dest, flags);
    }
}
