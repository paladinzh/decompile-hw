package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class i implements Creator<TmemberRight> {
    i() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public TmemberRight a(Parcel parcel) {
        TmemberRight tmemberRight = new TmemberRight();
        tmemberRight.a = parcel.readLong();
        tmemberRight.b = parcel.readInt();
        tmemberRight.c = parcel.readString();
        tmemberRight.d = parcel.readString();
        tmemberRight.e = parcel.readString();
        tmemberRight.f = parcel.readInt();
        tmemberRight.g = parcel.readString();
        tmemberRight.h = parcel.readString();
        return tmemberRight;
    }

    public TmemberRight[] a(int i) {
        return new TmemberRight[i];
    }
}
