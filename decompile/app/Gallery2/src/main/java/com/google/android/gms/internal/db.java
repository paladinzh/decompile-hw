package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class db implements SafeParcelable {
    public static final dc CREATOR = new dc();
    public String pU;
    public int pV;
    public int pW;
    public boolean pX;
    public final int versionCode;

    db(int i, String str, int i2, int i3, boolean z) {
        this.versionCode = i;
        this.pU = str;
        this.pV = i2;
        this.pW = i3;
        this.pX = z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        dc.a(this, out, flags);
    }
}
