package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;

/* compiled from: RouteSearch */
class n implements Creator<DriveRouteQuery> {
    n() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public DriveRouteQuery a(Parcel parcel) {
        return new DriveRouteQuery(parcel);
    }

    public DriveRouteQuery[] a(int i) {
        return new DriveRouteQuery[i];
    }
}
