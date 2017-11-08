package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: SubPoiItem */
class e implements Creator<SubPoiItem> {
    e() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public SubPoiItem a(Parcel parcel) {
        return new SubPoiItem(parcel);
    }

    public SubPoiItem[] a(int i) {
        return null;
    }
}
