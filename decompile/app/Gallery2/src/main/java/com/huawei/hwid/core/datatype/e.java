package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class e implements Creator<SMSCountryInfo> {
    e() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public SMSCountryInfo a(Parcel parcel) {
        SMSCountryInfo sMSCountryInfo = new SMSCountryInfo();
        sMSCountryInfo.a = parcel.readString();
        sMSCountryInfo.b = parcel.readString();
        sMSCountryInfo.c = parcel.readString();
        return sMSCountryInfo;
    }

    public SMSCountryInfo[] a(int i) {
        return new SMSCountryInfo[i];
    }
}
