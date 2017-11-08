package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: RegeocodeAddress */
class c implements Creator<RegeocodeAddress> {
    c() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public RegeocodeAddress a(Parcel parcel) {
        return new RegeocodeAddress(parcel);
    }

    public RegeocodeAddress[] a(int i) {
        return null;
    }
}
