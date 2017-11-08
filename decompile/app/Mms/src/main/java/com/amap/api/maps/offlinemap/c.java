package com.amap.api.maps.offlinemap;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: OfflineMapProvince */
class c implements Creator<OfflineMapProvince> {
    c() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public OfflineMapProvince a(Parcel parcel) {
        return new OfflineMapProvince(parcel);
    }

    public OfflineMapProvince[] a(int i) {
        return new OfflineMapProvince[i];
    }
}
