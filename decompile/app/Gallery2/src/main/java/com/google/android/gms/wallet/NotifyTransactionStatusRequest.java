package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class NotifyTransactionStatusRequest implements SafeParcelable {
    public static final Creator<NotifyTransactionStatusRequest> CREATOR = new m();
    String Yk;
    String Zk;
    int status;
    final int wj;

    NotifyTransactionStatusRequest() {
        this.wj = 1;
    }

    NotifyTransactionStatusRequest(int versionCode, String googleTransactionId, int status, String detailedReason) {
        this.wj = versionCode;
        this.Yk = googleTransactionId;
        this.status = status;
        this.Zk = detailedReason;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        m.a(this, out, flags);
    }
}
