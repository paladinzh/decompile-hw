package com.google.android.gms.drive;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class Contents implements SafeParcelable {
    public static final Creator<Contents> CREATOR = new a();
    final ParcelFileDescriptor AC;
    final int CQ;
    final int CR;
    final DriveId CS;
    private boolean CT = false;
    private boolean CU = false;
    private boolean mClosed = false;
    final int wj;

    Contents(int versionCode, ParcelFileDescriptor parcelFileDescriptor, int requestId, int mode, DriveId driveId) {
        this.wj = versionCode;
        this.AC = parcelFileDescriptor;
        this.CQ = requestId;
        this.CR = mode;
        this.CS = driveId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        a.a(this, dest, flags);
    }
}
