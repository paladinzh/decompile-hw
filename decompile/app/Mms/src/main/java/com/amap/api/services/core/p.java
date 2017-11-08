package com.amap.api.services.core;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: LatLonSharePoint */
class p implements Creator<LatLonSharePoint> {
    p() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public LatLonSharePoint a(Parcel parcel) {
        return new LatLonSharePoint(parcel);
    }

    public LatLonSharePoint[] a(int i) {
        return new LatLonSharePoint[i];
    }
}
