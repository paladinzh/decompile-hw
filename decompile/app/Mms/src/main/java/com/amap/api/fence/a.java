package com.amap.api.fence;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Fence */
class a implements Creator<Fence> {
    a() {
    }

    public Fence a(Parcel parcel) {
        return new Fence(parcel);
    }

    public Fence[] a(int i) {
        return new Fence[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }
}
