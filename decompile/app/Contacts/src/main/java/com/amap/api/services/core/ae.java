package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.WeatherSearchQuery;

/* compiled from: WeatherForecastHandler */
public class ae extends ag<WeatherSearchQuery, LocalWeatherForecast> {
    private LocalWeatherForecast h = new LocalWeatherForecast();

    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public /* bridge */ /* synthetic */ String g() {
        return super.g();
    }

    public ae(Context context, WeatherSearchQuery weatherSearchQuery) {
        super(context, weatherSearchQuery);
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("output=json");
        String city = ((WeatherSearchQuery) this.a).getCity();
        if (!n.i(city)) {
            stringBuffer.append("&city=").append(b(city));
        }
        stringBuffer.append("&extensions=all");
        stringBuffer.append("&key=" + aj.f(this.d));
        return stringBuffer.toString();
    }

    protected LocalWeatherForecast d(String str) throws AMapException {
        this.h = n.e(str);
        return this.h;
    }

    public LocalWeatherForecast h() {
        return this.h;
    }
}
