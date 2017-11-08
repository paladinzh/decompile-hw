package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: Agreement */
final class a implements Creator {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public Agreement a(Parcel parcel) {
        Agreement agreement = new Agreement();
        agreement.a = parcel.readString();
        agreement.e = parcel.readString();
        agreement.c = parcel.readString();
        agreement.f = parcel.readString();
        agreement.d = parcel.readString();
        agreement.g = parcel.readString();
        agreement.b = parcel.readString();
        agreement.h = parcel.readString();
        return agreement;
    }

    public Agreement[] a(int i) {
        return new Agreement[i];
    }
}
