package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class g implements Creator<SiteCountryInfo> {
    g() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public SiteCountryInfo a(Parcel parcel) {
        SiteCountryInfo siteCountryInfo = new SiteCountryInfo();
        siteCountryInfo.a = parcel.readString();
        siteCountryInfo.b = parcel.readString();
        siteCountryInfo.c = parcel.readString();
        siteCountryInfo.d = parcel.readString();
        siteCountryInfo.g = parcel.readString();
        siteCountryInfo.e = parcel.readInt();
        siteCountryInfo.f = parcel.readInt();
        siteCountryInfo.h = parcel.readInt();
        siteCountryInfo.i = parcel.readInt();
        siteCountryInfo.j = parcel.readInt();
        siteCountryInfo.k = parcel.readInt();
        siteCountryInfo.l = parcel.readInt();
        parcel.readStringList(siteCountryInfo.m);
        return siteCountryInfo;
    }

    public SiteCountryInfo[] a(int i) {
        return new SiteCountryInfo[i];
    }
}
