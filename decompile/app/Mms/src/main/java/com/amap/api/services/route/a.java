package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: BusPath */
class a implements Creator<BusPath> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public BusPath a(Parcel parcel) {
        return new BusPath(parcel);
    }

    public BusPath[] a(int i) {
        return null;
    }
}
