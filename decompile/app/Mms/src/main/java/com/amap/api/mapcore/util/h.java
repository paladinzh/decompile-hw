package com.amap.api.mapcore.util;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: CityObject */
class h implements Creator<g> {
    h() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public g a(Parcel parcel) {
        return new g(parcel);
    }

    public g[] a(int i) {
        return new g[i];
    }
}
