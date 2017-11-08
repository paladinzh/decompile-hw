package com.huawei.keyguard.events.weather;

public class WeatherDayInfo {
    protected String mDayCode;
    protected int mDayIndex;
    protected WeatherHalfDayInfo mDayTimeInfo = null;
    protected WeatherHalfDayInfo mNightTimeInfo = null;
    protected long mObsDate;
    protected long mSunRise;
    protected long mSunSet;

    public String toString() {
        return "[WeatherDayInfo] : mDayIndex = " + this.mDayIndex + ", mSunRise = " + this.mSunRise + ", mSunSet = " + this.mSunSet + ", mObsDate = " + this.mObsDate + ", mDayCode = " + this.mDayCode + ", mDayTimeInfo = " + this.mDayTimeInfo + ", mNightTimeInfo = " + this.mNightTimeInfo;
    }

    public WeatherDayInfo copy() {
        WeatherDayInfo info = new WeatherDayInfo();
        info.mDayIndex = this.mDayIndex;
        info.mSunRise = this.mSunRise;
        info.mSunSet = this.mSunSet;
        info.mObsDate = this.mObsDate;
        info.mDayCode = this.mDayCode;
        if (this.mDayTimeInfo != null) {
            info.mDayTimeInfo = this.mDayTimeInfo.copy();
        }
        if (this.mNightTimeInfo != null) {
            info.mNightTimeInfo = this.mNightTimeInfo.copy();
        }
        return info;
    }
}
