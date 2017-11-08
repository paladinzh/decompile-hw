package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Discount */
class c implements Creator<Discount> {
    c() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Discount a(Parcel parcel) {
        return new Discount(parcel);
    }

    public Discount[] a(int i) {
        return null;
    }
}
