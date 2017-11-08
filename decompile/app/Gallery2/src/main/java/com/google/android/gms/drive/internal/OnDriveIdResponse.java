package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.DriveId;

/* compiled from: Unknown */
public class OnDriveIdResponse implements SafeParcelable {
    public static final Creator<OnDriveIdResponse> CREATOR = new ab();
    DriveId Do;
    final int wj;

    OnDriveIdResponse(int versionCode, DriveId driveId) {
        this.wj = versionCode;
        this.Do = driveId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ab.a(this, dest, flags);
    }
}
