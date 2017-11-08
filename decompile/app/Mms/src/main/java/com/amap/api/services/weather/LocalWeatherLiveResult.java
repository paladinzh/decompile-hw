package com.amap.api.services.weather;

import com.amap.api.services.core.af;

public class LocalWeatherLiveResult {
    private WeatherSearchQuery a;
    private LocalWeatherLive b;

    static LocalWeatherLiveResult a(af afVar, LocalWeatherLive localWeatherLive) {
        return new LocalWeatherLiveResult(afVar, localWeatherLive);
    }

    private LocalWeatherLiveResult(af afVar, LocalWeatherLive localWeatherLive) {
        this.a = (WeatherSearchQuery) afVar.i();
        this.b = afVar.h();
    }

    public WeatherSearchQuery getWeatherLiveQuery() {
        return this.a;
    }

    public LocalWeatherLive getLiveResult() {
        return this.b;
    }
}
