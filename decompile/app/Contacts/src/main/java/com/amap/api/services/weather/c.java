package com.amap.api.services.weather;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: LocalWeatherLive */
class c implements Creator<LocalWeatherLive> {
    c() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public LocalWeatherLive a(Parcel parcel) {
        return new LocalWeatherLive(parcel);
    }

    public LocalWeatherLive[] a(int i) {
        return null;
    }
}
