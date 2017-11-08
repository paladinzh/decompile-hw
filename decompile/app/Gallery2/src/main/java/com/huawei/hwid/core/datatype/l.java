package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class l implements Creator<UserLoginInfo> {
    l() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public UserLoginInfo a(Parcel parcel) {
        UserLoginInfo userLoginInfo = new UserLoginInfo();
        userLoginInfo.h = parcel.readString();
        userLoginInfo.d = parcel.readString();
        userLoginInfo.f = parcel.readString();
        userLoginInfo.e = parcel.readString();
        userLoginInfo.g = parcel.readString();
        userLoginInfo.b = parcel.readString();
        userLoginInfo.c = parcel.readString();
        userLoginInfo.a = parcel.readString();
        return userLoginInfo;
    }

    public UserLoginInfo[] a(int i) {
        return new UserLoginInfo[i];
    }
}
