package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: UserAccountInfo */
final class k implements Creator {
    k() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public UserAccountInfo a(Parcel parcel) {
        UserAccountInfo userAccountInfo = new UserAccountInfo();
        userAccountInfo.a = parcel.readString();
        userAccountInfo.b = parcel.readString();
        userAccountInfo.c = parcel.readString();
        userAccountInfo.d = parcel.readString();
        userAccountInfo.e = parcel.readString();
        userAccountInfo.f = parcel.readString();
        userAccountInfo.g = parcel.readString();
        userAccountInfo.h = parcel.readString();
        userAccountInfo.i = parcel.readString();
        return userAccountInfo;
    }

    public UserAccountInfo[] a(int i) {
        return new UserAccountInfo[i];
    }
}
