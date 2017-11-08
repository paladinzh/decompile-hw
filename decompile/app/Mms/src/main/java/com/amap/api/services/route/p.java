package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.amap.api.services.route.RouteSearch.WalkRouteQuery;

/* compiled from: RouteSearch */
class p implements Creator<WalkRouteQuery> {
    p() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public WalkRouteQuery a(Parcel parcel) {
        return new WalkRouteQuery(parcel);
    }

    public WalkRouteQuery[] a(int i) {
        return new WalkRouteQuery[i];
    }
}
