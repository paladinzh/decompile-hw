package com.amap.api.services.geocoder;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: BusinessArea */
class a implements Creator<BusinessArea> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public BusinessArea a(Parcel parcel) {
        return new BusinessArea(parcel);
    }

    public BusinessArea[] a(int i) {
        return new BusinessArea[i];
    }
}
