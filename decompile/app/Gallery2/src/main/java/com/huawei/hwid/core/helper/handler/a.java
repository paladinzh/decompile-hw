package com.huawei.hwid.core.helper.handler;

import android.os.Parcel;
import android.os.Parcelable.Creator;

class a implements Creator<ErrorStatus> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public ErrorStatus a(Parcel parcel) {
        ErrorStatus errorStatus = new ErrorStatus();
        errorStatus.a = parcel.readInt();
        errorStatus.b = parcel.readString();
        return errorStatus;
    }

    public ErrorStatus[] a(int i) {
        return new ErrorStatus[i];
    }
}
