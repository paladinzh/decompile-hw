package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: StreetNumber */
class e implements Creator<StreetNumber> {
    e() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public StreetNumber a(Parcel parcel) {
        return new StreetNumber(parcel);
    }

    public StreetNumber[] a(int i) {
        return null;
    }
}
