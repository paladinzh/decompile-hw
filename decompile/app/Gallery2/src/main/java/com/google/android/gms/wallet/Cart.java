package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;

/* compiled from: Unknown */
public final class Cart implements SafeParcelable {
    public static final Creator<Cart> CREATOR = new b();
    String Yf;
    String Yg;
    ArrayList<LineItem> Yh;
    private final int wj;

    Cart() {
        this.wj = 1;
        this.Yh = new ArrayList();
    }

    Cart(int versionCode, String totalPrice, String currencyCode, ArrayList<LineItem> lineItems) {
        this.wj = versionCode;
        this.Yf = totalPrice;
        this.Yg = currencyCode;
        this.Yh = lineItems;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel dest, int flags) {
        b.a(this, dest, flags);
    }
}
