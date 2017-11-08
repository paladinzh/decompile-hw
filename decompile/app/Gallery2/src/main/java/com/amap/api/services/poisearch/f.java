package com.amap.api.services.poisearch;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Photo */
class f implements Creator<Photo> {
    f() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Photo a(Parcel parcel) {
        return new Photo(parcel);
    }

    public Photo[] a(int i) {
        return null;
    }
}
