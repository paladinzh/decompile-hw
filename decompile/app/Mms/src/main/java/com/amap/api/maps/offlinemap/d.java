package com.amap.api.maps.offlinemap;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Province */
class d implements Creator<Province> {
    d() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Province a(Parcel parcel) {
        return new Province(parcel);
    }

    public Province[] a(int i) {
        return new Province[i];
    }
}
