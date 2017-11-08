package com.google.android.gms.identity.intents.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class CountrySpecification implements SafeParcelable {
    public static final Creator<CountrySpecification> CREATOR = new a();
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
        a.a(this, dest, flags);
    }
}
