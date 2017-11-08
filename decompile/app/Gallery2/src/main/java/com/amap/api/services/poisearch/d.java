package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Groupbuy */
class d implements Creator<Groupbuy> {
    d() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Groupbuy a(Parcel parcel) {
        return new Groupbuy(parcel);
    }

    public Groupbuy[] a(int i) {
        return null;
    }
}
