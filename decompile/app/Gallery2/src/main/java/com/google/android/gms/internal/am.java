package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class am implements SafeParcelable {
    public static final an CREATOR = new an();
    public final int backgroundColor;
    public final int lI;
    public final int lJ;
    public final int lK;
    public final int lL;
    public final int lM;
    public final int lN;
    public final int lO;
    public final String lP;
    public final int lQ;
    public final String lR;
    public final int lS;
    public final int lT;
    public final String lU;
    public final int versionCode;

    am(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, String str, int i10, String str2, int i11, int i12, String str3) {
        this.versionCode = i;
        this.lI = i2;
        this.backgroundColor = i3;
        this.lJ = i4;
        this.lK = i5;
        this.lL = i6;
        this.lM = i7;
        this.lN = i8;
        this.lO = i9;
        this.lP = str;
        this.lQ = i10;
        this.lR = str2;
        this.lS = i11;
        this.lT = i12;
        this.lU = str3;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        an.a(this, out, flags);
    }
}
