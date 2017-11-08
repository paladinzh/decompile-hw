package com.amap.api.services.cloud;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: CloudItem */
class b implements Creator<CloudItem> {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public CloudItem a(Parcel parcel) {
        return new CloudItem(parcel);
    }

    public CloudItem[] a(int i) {
        return new CloudItem[i];
    }
}
