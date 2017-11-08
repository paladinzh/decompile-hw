package com.amap.api.services.district;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: DistrictResult */
class b implements Creator<DistrictResult> {
    final /* synthetic */ DistrictResult a;

    b(DistrictResult districtResult) {
        this.a = districtResult;
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public DistrictResult a(Parcel parcel) {
        return new DistrictResult(parcel);
    }

    public DistrictResult[] a(int i) {
        return new DistrictResult[i];
    }
}
