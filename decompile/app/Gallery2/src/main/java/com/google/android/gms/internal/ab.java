package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class ab implements SafeParcelable {
    public static final ac CREATOR = new ac();
    public final int height;
    public final int heightPixels;
    public final String ln;
    public final boolean lo;
    public final ab[] lp;
    public final int versionCode;
    public final int width;
    public final int widthPixels;

    public ab() {
        this(2, "interstitial_mb", 0, 0, true, 0, 0, null);
    }

    ab(int i, String str, int i2, int i3, boolean z, int i4, int i5, ab[] abVarArr) {
        this.versionCode = i;
        this.ln = str;
        this.height = i2;
        this.heightPixels = i3;
        this.lo = z;
        this.width = i4;
        this.widthPixels = i5;
        this.lp = abVarArr;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        ac.a(this, out, flags);
    }
}
