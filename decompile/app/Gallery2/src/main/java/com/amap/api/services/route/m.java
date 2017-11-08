package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.amap.api.services.route.RouteSearch.BusRouteQuery;

/* compiled from: RouteSearch */
class m implements Creator<BusRouteQuery> {
    m() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public BusRouteQuery a(Parcel parcel) {
        return new BusRouteQuery(parcel);
    }

    public BusRouteQuery[] a(int i) {
        return new BusRouteQuery[i];
    }
}
