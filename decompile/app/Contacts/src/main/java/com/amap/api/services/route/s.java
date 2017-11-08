package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: WalkPath */
class s implements Creator<WalkPath> {
    s() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public WalkPath a(Parcel parcel) {
        return new WalkPath(parcel);
    }

    public WalkPath[] a(int i) {
        return null;
    }
}
