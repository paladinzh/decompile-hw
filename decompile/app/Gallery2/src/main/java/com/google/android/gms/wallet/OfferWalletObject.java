package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class OfferWalletObject implements SafeParcelable {
    public static final Creator<OfferWalletObject> CREATOR = new n();
    String Zm;
    String eN;
    private final int wj;

    OfferWalletObject() {
        this.wj = 2;
    }

    OfferWalletObject(int versionCode, String id, String redemptionCode) {
        this.wj = versionCode;
        this.eN = id;
        this.Zm = redemptionCode;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel dest, int flags) {
        n.a(this, dest, flags);
    }
}
