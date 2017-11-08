package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: RouteBusWalkItem */
class k implements Creator<RouteBusWalkItem> {
    k() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public RouteBusWalkItem a(Parcel parcel) {
        return new RouteBusWalkItem(parcel);
    }

    public RouteBusWalkItem[] a(int i) {
        return null;
    }
}
