package com.amap.api.services.road;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Road */
class b implements Creator<Road> {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Road a(Parcel parcel) {
        return new Road(parcel);
    }

    public Road[] a(int i) {
        return null;
    }
}
