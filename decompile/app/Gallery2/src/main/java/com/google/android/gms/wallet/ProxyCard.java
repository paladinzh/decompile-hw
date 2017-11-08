package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class ProxyCard implements SafeParcelable {
    public static final Creator<ProxyCard> CREATOR = new o();
    String Zn;
    String Zo;
    int Zp;
    int Zq;
    private final int wj;

    ProxyCard(int versionCode, String pan, String cvn, int expirationMonth, int expirationYear) {
        this.wj = versionCode;
        this.Zn = pan;
        this.Zo = cvn;
        this.Zp = expirationMonth;
        this.Zq = expirationYear;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel out, int flags) {
        o.a(this, out, flags);
    }
}
