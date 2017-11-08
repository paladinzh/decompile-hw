package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class GetLocalNodeResponse implements SafeParcelable {
    public static final Creator<GetLocalNodeResponse> CREATOR = new zzas();
    public final int statusCode;
    public final int versionCode;
    public final NodeParcelable zzbaF;

    GetLocalNodeResponse(int versionCode, int statusCode, NodeParcelable node) {
        this.versionCode = versionCode;
        this.statusCode = statusCode;
        this.zzbaF = node;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzas.zza(this, dest, flags);
    }
}
