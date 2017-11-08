package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: District */
class d implements Creator<District> {
    d() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public District a(Parcel parcel) {
        return new District(parcel);
    }

    public District[] a(int i) {
        return null;
    }
}
