package com.autonavi.aps.amapapi.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: AmapLoc */
class a implements Creator<AmapLoc> {
    a() {
    }

    public AmapLoc a(Parcel parcel) {
        return new AmapLoc(parcel);
    }

    public AmapLoc[] a(int i) {
        return null;
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }
}
