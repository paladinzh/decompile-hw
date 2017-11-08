package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: AgreementVersion */
final class b implements Creator {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public AgreementVersion a(Parcel parcel) {
        AgreementVersion agreementVersion = new AgreementVersion();
        agreementVersion.a = parcel.readString();
        agreementVersion.c = parcel.readString();
        agreementVersion.b = parcel.readString();
        return agreementVersion;
    }

    public AgreementVersion[] a(int i) {
        return new AgreementVersion[i];
    }
}
