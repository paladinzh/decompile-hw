package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: SiteInfo */
final class i implements Creator {
    i() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public SiteInfo a(Parcel parcel) {
        SiteInfo siteInfo = new SiteInfo();
        siteInfo.a = parcel.readString();
        siteInfo.b = parcel.readString();
        return siteInfo;
    }

    public SiteInfo[] a(int i) {
        return new SiteInfo[i];
    }
}
