package com.huawei.keyguard.events.weather;

public class DispalyWeatherInfo {
    protected int mHighTemperature = -20000;
    protected int mLowTemperature = -20000;
    protected long mObsDate;
    protected int mStatus = 0;
    protected long mSunRise;
    protected long mSunSet;

    public boolean isNight(long current, int dayIndex) {
        long ralativeTime = current + (((long) dayIndex) * 86400000);
        if (ralativeTime < this.mSunRise || ralativeTime > this.mSunSet) {
            return true;
        }
        return false;
    }

    public String toString() {
        return "[DispalyWeatherInfo] : mObsDate = " + this.mObsDate + ", mStatus = " + this.mStatus + ", mHighTemperature = " + this.mHighTemperature + ", mLowTemperature = " + this.mLowTemperature;
    }
}
