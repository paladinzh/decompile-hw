package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: GeocodeAddress */
class b implements Creator<GeocodeAddress> {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public GeocodeAddress[] a(int i) {
        return null;
    }

    public GeocodeAddress a(Parcel parcel) {
        return new GeocodeAddress(parcel);
    }
}
