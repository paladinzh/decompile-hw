package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class k implements Creator<UserInfo> {
    k() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public UserInfo a(Parcel parcel) {
        UserInfo userInfo = new UserInfo();
        userInfo.i = parcel.readString();
        userInfo.h = parcel.readString();
        userInfo.n = parcel.readString();
        userInfo.q = parcel.readString();
        userInfo.d = parcel.readString();
        userInfo.g = parcel.readString();
        userInfo.k = parcel.readString();
        userInfo.c = parcel.readString();
        userInfo.e = parcel.readString();
        userInfo.l = parcel.readString();
        userInfo.a = parcel.readString();
        userInfo.b = parcel.readString();
        userInfo.j = parcel.readString();
        userInfo.p = parcel.readString();
        userInfo.o = parcel.readString();
        userInfo.m = parcel.readString();
        userInfo.r = parcel.readString();
        userInfo.f = parcel.readString();
        userInfo.s = parcel.readString();
        userInfo.t = parcel.readString();
        userInfo.u = parcel.readString();
        userInfo.v = parcel.readString();
        userInfo.w = parcel.readString();
        userInfo.x = parcel.readString();
        userInfo.y = parcel.readString();
        userInfo.z = parcel.readString();
        userInfo.A = parcel.readString();
        userInfo.B = parcel.readString();
        userInfo.D = parcel.readString();
        userInfo.C = parcel.readString();
        userInfo.E = parcel.readString();
        userInfo.F = parcel.readString();
        userInfo.I = parcel.readString();
        userInfo.G = parcel.readString();
        userInfo.H = parcel.readString();
        return userInfo;
    }

    public UserInfo[] a(int i) {
        return new UserInfo[i];
    }
}
