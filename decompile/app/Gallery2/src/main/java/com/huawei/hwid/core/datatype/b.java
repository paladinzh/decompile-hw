package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class b implements Creator<ChildrenInfo> {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public ChildrenInfo a(Parcel parcel) {
        ChildrenInfo childrenInfo = new ChildrenInfo();
        childrenInfo.a = parcel.readString();
        childrenInfo.b = parcel.readString();
        childrenInfo.c = parcel.readString();
        childrenInfo.d = parcel.readString();
        childrenInfo.e = parcel.readString();
        childrenInfo.f = parcel.readString();
        childrenInfo.g = parcel.readString();
        return childrenInfo;
    }

    public ChildrenInfo[] a(int i) {
        return new ChildrenInfo[i];
    }
}
