package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: StreetNumber */
class f implements Creator<StreetNumber> {
    f() {
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
