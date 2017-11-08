package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: AoiItem */
class a implements Creator<AoiItem> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public AoiItem a(Parcel parcel) {
        return new AoiItem(parcel);
    }

    public AoiItem[] a(int i) {
        return new AoiItem[i];
    }
}
