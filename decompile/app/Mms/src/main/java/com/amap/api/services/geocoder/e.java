package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: RegeocodeRoad */
class e implements Creator<RegeocodeRoad> {
    e() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public RegeocodeRoad a(Parcel parcel) {
        return new RegeocodeRoad(parcel);
    }

    public RegeocodeRoad[] a(int i) {
        return null;
    }
}
