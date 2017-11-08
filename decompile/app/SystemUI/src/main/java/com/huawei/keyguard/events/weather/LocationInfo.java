package com.huawei.keyguard.events.weather;

import com.huawei.keyguard.util.HwLog;
import java.util.TimeZone;

public class LocationInfo {
    private WeatherInfo mCurrentWeatherInfo;
    private WeatherInfo mHomeWeatherInfo;

    public WeatherInfo getCurrentWeatherInfo() {
        return this.mCurrentWeatherInfo;
    }

    public void setCurrentWeatherInfo(WeatherInfo currentWeatherInfo) {
        this.mCurrentWeatherInfo = currentWeatherInfo;
    }

    public void setHomeWeatherInfo(WeatherInfo homeWeatherInfo) {
        this.mHomeWeatherInfo = homeWeatherInfo;
    }

    public WeatherInfo getHomeWeatherInfo() {
        return this.mHomeWeatherInfo;
    }

    public String getHomeLocation() {
        if (this.mHomeWeatherInfo != null) {
            return this.mHomeWeatherInfo.getCityName();
        }
        HwLog.d("WeatherLocationMonitor", "homeWeatherInfo is null");
        return null;
    }

    public TimeZone getHomeTimeZone() {
        return this.mHomeWeatherInfo == null ? null : this.mHomeWeatherInfo.getTimeZone();
    }
}
