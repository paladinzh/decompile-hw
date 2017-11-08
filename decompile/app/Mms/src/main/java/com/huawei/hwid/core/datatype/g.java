package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: PhoneNumInfo */
final class g implements Creator {
    g() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public PhoneNumInfo a(Parcel parcel) {
        PhoneNumInfo phoneNumInfo = new PhoneNumInfo();
        phoneNumInfo.b = parcel.readString();
        phoneNumInfo.a = parcel.readString();
        phoneNumInfo.c = parcel.readString();
        return phoneNumInfo;
    }

    public PhoneNumInfo[] a(int i) {
        return new PhoneNumInfo[i];
    }
}
