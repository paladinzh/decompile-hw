package com.amap.api.services.cloud;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: CloudItemDetail */
class c implements Creator<CloudItemDetail> {
    c() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public CloudItemDetail a(Parcel parcel) {
        return new CloudItemDetail(parcel);
    }

    public CloudItemDetail[] a(int i) {
        return new CloudItemDetail[i];
    }
}
