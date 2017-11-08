package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Cinema */
class a implements Creator<Cinema> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Cinema a(Parcel parcel) {
        return new Cinema(parcel);
    }

    public Cinema[] a(int i) {
        return null;
    }
}
