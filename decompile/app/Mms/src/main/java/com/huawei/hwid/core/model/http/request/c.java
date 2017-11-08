package com.huawei.hwid.core.model.http.request;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: AgreementVersion */
final class c implements Creator {
    c() {
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
        agreementVersion.d = parcel.readString();
        agreementVersion.e = parcel.readString();
        agreementVersion.f = parcel.readString();
        agreementVersion.g = parcel.readString();
        agreementVersion.h = parcel.readString();
        return agreementVersion;
    }

    public AgreementVersion[] a(int i) {
        return new AgreementVersion[i];
    }
}
