package com.google.android.gms.drive.events;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.DriveId;

/* compiled from: Unknown */
public final class ChangeEvent implements SafeParcelable, ResourceEvent {
    public static final Creator<ChangeEvent> CREATOR = new a();
    final DriveId CS;
    final int Dl;
    final int wj;

    ChangeEvent(int versionCode, DriveId driveId, int changeFlags) {
        this.wj = versionCode;
        this.CS = driveId;
        this.Dl = changeFlags;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return String.format("ChangeEvent [id=%s,changeFlags=%x]", new Object[]{this.CS, Integer.valueOf(this.Dl)});
    }

    public void writeToParcel(Parcel dest, int flags) {
        a.a(this, dest, flags);
    }
}
