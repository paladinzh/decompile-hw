package com.amap.api.services.weather;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: LocalDayWeatherForecast */
class a implements Creator<LocalDayWeatherForecast> {
    a() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public LocalDayWeatherForecast a(Parcel parcel) {
        return new LocalDayWeatherForecast(parcel);
    }

    public LocalDayWeatherForecast[] a(int i) {
        return null;
    }
}
