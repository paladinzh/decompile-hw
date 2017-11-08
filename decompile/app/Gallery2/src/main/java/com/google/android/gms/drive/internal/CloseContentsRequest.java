package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.Contents;

/* compiled from: Unknown */
public class CloseContentsRequest implements SafeParcelable {
    public static final Creator<CloseContentsRequest> CREATOR = new e();
    final Contents Dq;
    final Boolean Dr;
    final int wj;

    CloseContentsRequest(int versionCode, Contents contentsReference, Boolean saveResults) {
        this.wj = versionCode;
        this.Dq = contentsReference;
        this.Dr = saveResults;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        e.a(this, dest, flags);
    }
}
