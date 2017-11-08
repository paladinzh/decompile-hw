package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class jr implements SafeParcelable {
    public static final Creator<jr> CREATOR = new js();
    long ZL;
    long ZM;
    private final int wj;

    jr() {
        this.wj = 1;
    }

    jr(int i, long j, long j2) {
        this.wj = i;
        this.ZL = j;
        this.ZM = j2;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel dest, int flags) {
        js.a(this, dest, flags);
    }
}
