package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: DeviceInfo */
final class d implements Creator {
    d() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public DeviceInfo a(Parcel parcel) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.b = parcel.readString();
        deviceInfo.d = parcel.readString();
        deviceInfo.a = parcel.readString();
        deviceInfo.c = parcel.readString();
        deviceInfo.e = parcel.readString();
        deviceInfo.f = parcel.readString();
        deviceInfo.g = parcel.readString();
        return deviceInfo;
    }

    public DeviceInfo[] a(int i) {
        return new DeviceInfo[i];
    }
}
