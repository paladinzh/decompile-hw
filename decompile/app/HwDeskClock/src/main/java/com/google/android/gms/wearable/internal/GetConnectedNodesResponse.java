package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.List;

/* compiled from: Unknown */
public class GetConnectedNodesResponse implements SafeParcelable {
    public static final Creator<GetConnectedNodesResponse> CREATOR = new zzap();
    public final int statusCode;
    public final int versionCode;
    public final List<NodeParcelable> zzbaC;

    GetConnectedNodesResponse(int versionCode, int statusCode, List<NodeParcelable> nodes) {
        this.versionCode = versionCode;
        this.statusCode = statusCode;
        this.zzbaC = nodes;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzap.zza(this, dest, flags);
    }
}
