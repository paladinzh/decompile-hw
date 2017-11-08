package com.amap.api.services.cloud;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: CloudImage */
class a implements Creator<CloudImage> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public CloudImage a(Parcel parcel) {
        return new CloudImage(parcel);
    }

    public CloudImage[] a(int i) {
        return new CloudImage[i];
    }
}
