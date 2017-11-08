package com.amap.api.services.weather;

import android.os.Parcel;
import android.os.Parcelable.Creator;

/* compiled from: LocalWeatherForecast */
class b implements Creator<LocalWeatherForecast> {
    b() {
    }

    public /* synthetic */ Object createFromParcel(Parcel parcel) {
        return a(parcel);
    }

    public /* synthetic */ Object[] newArray(int i) {
        return a(i);
    }

    public LocalWeatherForecast a(Parcel parcel) {
        return new LocalWeatherForecast(parcel);
    }

    public LocalWeatherForecast[] a(int i) {
        return null;
    }
}
