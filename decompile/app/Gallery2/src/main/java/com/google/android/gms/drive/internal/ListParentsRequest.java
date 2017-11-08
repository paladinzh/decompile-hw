package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.DriveId;

/* compiled from: Unknown */
public class ListParentsRequest implements SafeParcelable {
    public static final Creator<ListParentsRequest> CREATOR = new x();
    final DriveId DT;
    final int wj;

    ListParentsRequest(int versionCode, DriveId driveId) {
        this.wj = versionCode;
        this.DT = driveId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        x.a(this, dest, flags);
    }
}
