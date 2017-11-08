package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.WeatherSearchQuery;

/* compiled from: WeatherLiveHandler */
public class af extends ag<WeatherSearchQuery, LocalWeatherLive> {
    private LocalWeatherLive h = new LocalWeatherLive();

    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public /* bridge */ /* synthetic */ String g() {
        return super.g();
    }

    public af(Context context, WeatherSearchQuery weatherSearchQuery) {
        super(context, weatherSearchQuery);
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json");
        String city = ((WeatherSearchQuery) this.a).getCity();
        if (!n.i(city)) {
            stringBuffer.append("&city=").append(b(city));
        }
        stringBuffer.append("&extensions=base");
        stringBuffer.append("&key=" + aj.f(this.d));
        return stringBuffer.toString();
    }

    protected LocalWeatherLive d(String str) throws AMapException {
        this.h = n.d(str);
        return this.h;
    }

    public LocalWeatherLive h() {
        return this.h;
    }
}
