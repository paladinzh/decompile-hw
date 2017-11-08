package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Dining */
class b implements Creator<Dining> {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Dining a(Parcel parcel) {
        return new Dining(parcel);
    }

    public Dining[] a(int i) {
        return null;
    }
}
