package com.amap.api.services.busline;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: BusStationItem */
class b implements Creator<BusStationItem> {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public BusStationItem a(Parcel parcel) {
        return new BusStationItem(parcel);
    }

    public BusStationItem[] a(int i) {
        return null;
    }
}
