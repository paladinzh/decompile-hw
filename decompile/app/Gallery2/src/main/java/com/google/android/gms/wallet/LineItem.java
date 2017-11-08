package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class LineItem implements SafeParcelable {
    public static final Creator<LineItem> CREATOR = new i();
    int YA;
    String Yf;
    String Yg;
    String Yy;
    String Yz;
    String description;
    private final int wj;

    LineItem() {
        this.wj = 1;
        this.YA = 0;
    }

    LineItem(int versionCode, String description, String quantity, String unitPrice, String totalPrice, int role, String currencyCode) {
        this.wj = versionCode;
        this.description = description;
        this.Yy = quantity;
        this.Yz = unitPrice;
        this.Yf = totalPrice;
        this.YA = role;
        this.Yg = currencyCode;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel dest, int flags) {
        i.a(this, dest, flags);
    }
}
