package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class h implements Creator<SiteListInfo> {
    h() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public SiteListInfo a(Parcel parcel) {
        SiteListInfo siteListInfo = new SiteListInfo();
        siteListInfo.a = parcel.readInt();
        siteListInfo.b = parcel.readString();
        siteListInfo.c = parcel.readString();
        siteListInfo.d = parcel.readInt();
        siteListInfo.e = parcel.readInt();
        siteListInfo.f = parcel.readInt();
        siteListInfo.g = parcel.readInt();
        siteListInfo.h = parcel.readInt();
        parcel.readStringList(siteListInfo.i);
        return siteListInfo;
    }

    public SiteListInfo[] a(int i) {
        return new SiteListInfo[i];
    }
}
