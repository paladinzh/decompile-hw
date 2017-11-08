package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class GetCloudSyncOptInStatusResponse implements SafeParcelable {
    public static final Creator<GetCloudSyncOptInStatusResponse> CREATOR = new zzal();
    public final int statusCode;
    public final int versionCode;
    public final boolean zzbay;
    public final boolean zzbaz;

    GetCloudSyncOptInStatusResponse(int versionCode, int statusCode, boolean isOptInOrOutDone, boolean isOptedIn) {
        this.versionCode = versionCode;
        this.statusCode = statusCode;
        this.zzbay = isOptInOrOutDone;
        this.zzbaz = isOptedIn;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzal.zza(this, dest, flags);
    }
}
