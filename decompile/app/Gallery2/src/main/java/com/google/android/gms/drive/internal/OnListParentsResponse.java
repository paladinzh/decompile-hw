package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.data.DataHolder;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class OnListParentsResponse implements SafeParcelable {
    public static final Creator<OnListParentsResponse> CREATOR = new ae();
    final DataHolder Ee;
    final int wj;

    OnListParentsResponse(int versionCode, DataHolder parents) {
        this.wj = versionCode;
        this.Ee = parents;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ae.a(this, dest, flags);
    }
}
