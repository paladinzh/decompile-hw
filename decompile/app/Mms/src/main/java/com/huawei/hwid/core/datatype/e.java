package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: EmailInfo */
final class e implements Creator {
    e() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public EmailInfo a(Parcel parcel) {
        EmailInfo emailInfo = new EmailInfo();
        emailInfo.a = parcel.readString();
        emailInfo.b = parcel.readString();
        return emailInfo;
    }

    public EmailInfo[] a(int i) {
        return new EmailInfo[i];
    }
}
