package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Scenic */
class k implements Creator<Scenic> {
    k() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Scenic a(Parcel parcel) {
        return new Scenic(parcel);
    }

    public Scenic[] a(int i) {
        return null;
    }
}
