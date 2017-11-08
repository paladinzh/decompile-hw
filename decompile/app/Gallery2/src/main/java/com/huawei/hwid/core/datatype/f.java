package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class f implements Creator<SMSKeyInfo> {
    f() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public SMSKeyInfo a(Parcel parcel) {
        SMSKeyInfo sMSKeyInfo = new SMSKeyInfo();
        sMSKeyInfo.a = parcel.readString();
        sMSKeyInfo.b = parcel.readString();
        return sMSKeyInfo;
    }

    public SMSKeyInfo[] a(int i) {
        return new SMSKeyInfo[i];
    }
}
