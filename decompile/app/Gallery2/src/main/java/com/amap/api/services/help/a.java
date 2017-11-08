package com.amap.api.services.help;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Tip */
class a implements Creator<Tip> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Tip a(Parcel parcel) {
        return new Tip(parcel);
    }

    public Tip[] a(int i) {
        return null;
    }
}
