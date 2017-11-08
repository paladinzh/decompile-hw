package com.amap.api.maps.offlinemap;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: City */
class a implements Creator<City> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public City a(Parcel parcel) {
        return new City(parcel);
    }

    public City[] a(int i) {
        return new City[i];
    }
}
