package com.amap.api.services.weather;

import com.amap.api.services.core.ae;

public class LocalWeatherForecastResult {
    private WeatherSearchQuery a;
    private LocalWeatherForecast b;

    static LocalWeatherForecastResult a(ae aeVar, LocalWeatherForecast localWeatherForecast) {
        return new LocalWeatherForecastResult(aeVar, localWeatherForecast);
    }

    private LocalWeatherForecastResult(ae aeVar, LocalWeatherForecast localWeatherForecast) {
        this.a = (WeatherSearchQuery) aeVar.i();
        this.b = aeVar.h();
    }

    public WeatherSearchQuery getWeatherForecastQuery() {
        return this.a;
    }

    public LocalWeatherForecast getForecastResult() {
        return this.b;
    }
}
