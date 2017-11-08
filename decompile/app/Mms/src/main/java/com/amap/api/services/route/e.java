package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Doorway */
class e implements Creator<Doorway> {
    e() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Doorway a(Parcel parcel) {
        return new Doorway(parcel);
    }

    public Doorway[] a(int i) {
        return null;
    }
}
