package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: WalkRouteResult */
class t implements Creator<WalkRouteResult> {
    t() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public WalkRouteResult a(Parcel parcel) {
        return new WalkRouteResult(parcel);
    }

    public WalkRouteResult[] a(int i) {
        return new WalkRouteResult[i];
    }
}
