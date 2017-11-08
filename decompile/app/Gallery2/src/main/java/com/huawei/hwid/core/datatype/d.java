package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class d implements Creator<HwAccount> {
    d() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public HwAccount a(Parcel parcel) {
        HwAccount hwAccount = new HwAccount();
        hwAccount.a = parcel.readString();
        hwAccount.b = parcel.readString();
        hwAccount.c = parcel.readString();
        hwAccount.d = parcel.readString();
        hwAccount.e = parcel.readInt();
        hwAccount.f = parcel.readString();
        hwAccount.g = parcel.readString();
        hwAccount.h = parcel.readString();
        hwAccount.i = parcel.readString();
        hwAccount.j = parcel.readString();
        hwAccount.k = parcel.readString();
        return hwAccount;
    }

    public HwAccount[] a(int i) {
        return new HwAccount[i];
    }
}
