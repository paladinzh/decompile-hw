package com.huawei.keyguard.events.weather;

public class WeatherHalfDayInfo {
    public float mHighTemperature = -20000.0f;
    public float mLowTemperature = -20000.0f;
    public int mWeatherIcon;

    public String toString() {
        return "[WeatherHalfDayInfo] : mHighTemperature = " + this.mHighTemperature + ", mLowTemperature = " + this.mLowTemperature + ", mWeatherIcon = " + this.mWeatherIcon;
    }

    public WeatherHalfDayInfo copy() {
        WeatherHalfDayInfo info = new WeatherHalfDayInfo();
        info.mHighTemperature = this.mHighTemperature;
        info.mLowTemperature = this.mLowTemperature;
        info.mWeatherIcon = this.mWeatherIcon;
        return info;
    }
}
