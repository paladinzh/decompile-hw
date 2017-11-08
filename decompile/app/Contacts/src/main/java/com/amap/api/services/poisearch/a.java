package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: IndoorData */
class a implements Creator<IndoorData> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public IndoorData a(Parcel parcel) {
        return new IndoorData(parcel);
    }

    public IndoorData[] a(int i) {
        return new IndoorData[i];
    }
}
