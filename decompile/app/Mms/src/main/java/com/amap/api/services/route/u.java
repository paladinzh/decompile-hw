package com.amap.api.services.route;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: WalkStep */
class u implements Creator<WalkStep> {
    u() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public WalkStep a(Parcel parcel) {
        return new WalkStep(parcel);
    }

    public WalkStep[] a(int i) {
        return null;
    }
}
