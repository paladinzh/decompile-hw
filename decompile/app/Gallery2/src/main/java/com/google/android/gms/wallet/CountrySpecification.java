package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

@Deprecated
/* compiled from: Unknown */
public class CountrySpecification implements SafeParcelable {
    public static final Creator<CountrySpecification> CREATOR = new c();
    String oQ;
    private final int wj;

    CountrySpecification(int versionCode, String countryCode) {
        this.wj = versionCode;
        this.oQ = countryCode;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel dest, int flags) {
        c.a(this, dest, flags);
    }
}
