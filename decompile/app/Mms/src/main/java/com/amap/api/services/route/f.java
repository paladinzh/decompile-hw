package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: DrivePath */
class f implements Creator<DrivePath> {
    f() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public DrivePath a(Parcel parcel) {
        return new DrivePath(parcel);
    }

    public DrivePath[] a(int i) {
        return null;
    }
}
