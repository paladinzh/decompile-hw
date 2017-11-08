package com.amap.api.services.core;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: LatLonPoint */
class o implements Creator<LatLonPoint> {
    o() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public LatLonPoint a(Parcel parcel) {
        return new LatLonPoint(parcel);
    }

    public LatLonPoint[] a(int i) {
        return new LatLonPoint[i];
    }
}
