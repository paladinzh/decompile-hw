package com.amap.api.services.core;

import android.content.Context;

/* compiled from: WeatherSearchHandler */
abstract class ag<T, V> extends b<T, V> {
    public ag(Context context, T t) {
        super(context, t);
    }

    public T i() {
        return this.a;
    }

    public String g() {
        return h.a() + "/weather/weatherInfo?";
    }
}
