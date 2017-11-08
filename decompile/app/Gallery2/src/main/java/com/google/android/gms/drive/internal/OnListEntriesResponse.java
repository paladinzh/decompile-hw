package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class OnListEntriesResponse implements SafeParcelable {
    public static final Creator<OnListEntriesResponse> CREATOR = new ad();
    final DataHolder Ed;
    final int wj;

    OnListEntriesResponse(int versionCode, DataHolder entries) {
        this.wj = versionCode;
        this.Ed = entries;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ad.a(this, dest, flags);
    }
}
