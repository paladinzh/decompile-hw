package com.amap.api.services.district;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: DistrictSearchQuery */
class c implements Creator<DistrictSearchQuery> {
    c() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public DistrictSearchQuery a(Parcel parcel) {
        boolean z;
        boolean z2 = false;
        DistrictSearchQuery districtSearchQuery = new DistrictSearchQuery();
        districtSearchQuery.setKeywords(parcel.readString());
        districtSearchQuery.setKeywordsLevel(parcel.readString());
        districtSearchQuery.setPageNum(parcel.readInt());
        districtSearchQuery.setPageSize(parcel.readInt());
        if (parcel.readByte() != (byte) 1) {
            z = false;
        } else {
            z = true;
        }
        districtSearchQuery.setShowChild(z);
        if (parcel.readByte() == (byte) 1) {
            z2 = true;
        }
        districtSearchQuery.setShowBoundary(z2);
        return districtSearchQuery;
    }

    public DistrictSearchQuery[] a(int i) {
        return new DistrictSearchQuery[i];
    }
}
