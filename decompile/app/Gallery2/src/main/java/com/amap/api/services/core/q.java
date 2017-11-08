package com.amap.api.services.core;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: PoiItem */
class q implements Creator<PoiItem> {
    q() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public PoiItem a(Parcel parcel) {
        return new PoiItem(parcel);
    }

    public PoiItem[] a(int i) {
        return new PoiItem[i];
    }
}
