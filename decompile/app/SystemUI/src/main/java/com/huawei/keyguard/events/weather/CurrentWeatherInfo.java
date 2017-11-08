package com.huawei.keyguard.events.weather;

public class CurrentWeatherInfo {
    protected long mObservationTime;
    protected float mTemperature = -20000.0f;
    protected int mWeatherIcon;

    public CurrentWeatherInfo copy() {
        CurrentWeatherInfo info = new CurrentWeatherInfo();
        info.mTemperature = this.mTemperature;
        info.mObservationTime = this.mObservationTime;
        info.mWeatherIcon = this.mWeatherIcon;
        return info;
    }

    protected float getTemperature() {
        return this.mTemperature;
    }

    protected long getObservationTime() {
        return this.mObservationTime;
    }

    protected int getWeatherIcon() {
        return this.mWeatherIcon;
    }
}
