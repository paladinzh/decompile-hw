package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.Contents;

/* compiled from: Unknown */
public class OnContentsResponse implements SafeParcelable {
    public static final Creator<OnContentsResponse> CREATOR = new z();
    final Contents CW;
    final int wj;

    OnContentsResponse(int versionCode, Contents contents) {
        this.wj = versionCode;
        this.CW = contents;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        z.a(this, dest, flags);
    }
}
