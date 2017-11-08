package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class OnDownloadProgressResponse implements SafeParcelable {
    public static final Creator<OnDownloadProgressResponse> CREATOR = new aa();
    final long DZ;
    final long Ea;
    final int wj;

    OnDownloadProgressResponse(int versionCode, long bytesLoaded, long bytesExpected) {
        this.wj = versionCode;
        this.DZ = bytesLoaded;
        this.Ea = bytesExpected;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        aa.a(this, dest, flags);
    }
}
