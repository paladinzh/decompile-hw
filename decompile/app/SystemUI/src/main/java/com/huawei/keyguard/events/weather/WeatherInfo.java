package com.huawei.keyguard.events.weather;

import android.util.SparseArray;
import com.huawei.keyguard.util.HwLog;
import java.util.TimeZone;

public class WeatherInfo {
    public CityInfo mCityInfo;
    public CurrentWeatherInfo mCurrentWeatherInfo;
    private boolean mDualClockSwitchFromWeather;
    public int mTempType = 0;
    SparseArray<WeatherDayInfo> mWeatherDayInfo = null;

    public WeatherInfo copy() {
        WeatherInfo info = new WeatherInfo();
        if (this.mCityInfo != null) {
            info.mCityInfo = this.mCityInfo.copy();
        }
        info.mTempType = this.mTempType;
        if (this.mCurrentWeatherInfo != null) {
            info.mCurrentWeatherInfo = this.mCurrentWeatherInfo.copy();
        }
        if (this.mWeatherDayInfo != null) {
            info.mWeatherDayInfo = new SparseArray();
            for (int i = 0; i < this.mWeatherDayInfo.size(); i++) {
                WeatherDayInfo srcInfo = (WeatherDayInfo) this.mWeatherDayInfo.get(i);
                if (srcInfo != null) {
                    info.mWeatherDayInfo.put(i, srcInfo.copy());
                }
            }
        }
        return info;
    }

    public void setSwich(boolean state) {
        this.mDualClockSwitchFromWeather = state;
    }

    public String getCityName() {
        if (this.mCityInfo != null) {
            return this.mCityInfo.getDisplayName();
        }
        HwLog.e("WeatherInfo", "getCityName : mCityInfo is null ");
        return null;
    }

    public TimeZone getTimeZone() {
        if (this.mCityInfo != null) {
            return this.mCityInfo.getTimeZone();
        }
        HwLog.e("WeatherInfo", "getTimeZone : mCityInfo is null ");
        return TimeZone.getDefault();
    }

    public int getCurrentTemperture() {
        if (this.mCurrentWeatherInfo == null) {
            HwLog.e("WeatherInfo", "getCurrentTemperture : mCurrentWeatherInfo is null ");
            return -20000;
        }
        float temp = -20000.0f;
        float currentTemp = this.mCurrentWeatherInfo.getTemperature();
        long obsTime = this.mCurrentWeatherInfo.getObservationTime();
        long now = System.currentTimeMillis();
        int invalidDay = WeatherUtils.getRelativeDay(now, obsTime, getTimeZone());
        if (invalidDay >= 0 && invalidDay < 5 && this.mWeatherDayInfo != null) {
            WeatherDayInfo info = (WeatherDayInfo) this.mWeatherDayInfo.get(invalidDay);
            if (info != null) {
                WeatherHalfDayInfo half;
                if (now < info.mSunRise || now > info.mSunSet) {
                    half = info.mNightTimeInfo;
                } else {
                    half = info.mDayTimeInfo;
                }
                temp = (invalidDay != 0 || currentTemp == -20000.0f) ? (half.mLowTemperature + half.mHighTemperature) / 2.0f : currentTemp < half.mLowTemperature ? half.mLowTemperature : currentTemp > half.mHighTemperature ? half.mHighTemperature : currentTemp;
            }
        }
        if (temp == -20000.0f) {
            HwLog.w("WeatherInfo", "getCurrentTemperture get INVAILD_TEMPERTURE!");
            return (int) temp;
        }
        HwLog.i("WeatherInfo", "getCurrentTemperture get temp is: " + temp);
        return WeatherUtils.getTemperture(temp, this.mTempType);
    }

    public int getCurrentWeatherStatus() {
        return WeatherUtils.getWeatherStatusByWeatherIcon(getCurrentWeathericonIndex());
    }

    public int getCurrentWeathericonIndex() {
        int iconIndex = -1;
        if (this.mCurrentWeatherInfo == null) {
            HwLog.e("WeatherInfo", "getCurrentWeatherStatus : mCurrentWeatherInfo is null ");
            return -1;
        }
        long obsTime = this.mCurrentWeatherInfo.getObservationTime();
        long now = System.currentTimeMillis();
        int invalidDay = WeatherUtils.getRelativeDay(now, obsTime, getTimeZone());
        if (invalidDay == 0) {
            iconIndex = this.mCurrentWeatherInfo.getWeatherIcon();
        } else if (invalidDay > 0 && invalidDay < 5 && this.mWeatherDayInfo != null) {
            WeatherDayInfo info = (WeatherDayInfo) this.mWeatherDayInfo.get(invalidDay);
            if (info != null) {
                WeatherHalfDayInfo half;
                if (now < info.mSunRise || now > info.mSunSet) {
                    half = info.mNightTimeInfo;
                } else {
                    half = info.mDayTimeInfo;
                }
                iconIndex = half.mWeatherIcon;
            }
        }
        return iconIndex;
    }

    public DispalyWeatherInfo getDispalyWeatherInfo(long currentTime, int dayIndex) {
        if (this.mWeatherDayInfo == null) {
            HwLog.e("WeatherInfo", "getDispalyWeatherInfo : mWeatherDayInfo is null ");
            return null;
        }
        int relative = -1;
        long ralativeTime = currentTime + (((long) dayIndex) * 86400000);
        if (this.mWeatherDayInfo.get(0) != null) {
            relative = WeatherUtils.getRelativeDay(ralativeTime, ((WeatherDayInfo) this.mWeatherDayInfo.get(0)).mObsDate, getTimeZone());
        }
        if (relative >= 0 && relative < 5) {
            DispalyWeatherInfo displayInfo = new DispalyWeatherInfo();
            WeatherDayInfo info = (WeatherDayInfo) this.mWeatherDayInfo.get(relative);
            if (info != null) {
                WeatherHalfDayInfo half;
                int i;
                displayInfo.mObsDate = info.mObsDate;
                displayInfo.mSunRise = info.mSunRise;
                displayInfo.mSunSet = info.mSunSet;
                if (ralativeTime < info.mSunRise || ralativeTime > info.mSunSet) {
                    half = info.mNightTimeInfo;
                } else {
                    half = info.mDayTimeInfo;
                }
                if (half == null || half.mHighTemperature == -20000.0f) {
                    i = -20000;
                } else {
                    i = WeatherUtils.getTemperture(half.mHighTemperature, this.mTempType);
                }
                displayInfo.mHighTemperature = i;
                if (half == null || half.mLowTemperature == -20000.0f) {
                    i = -20000;
                } else {
                    i = WeatherUtils.getTemperture(half.mLowTemperature, this.mTempType);
                }
                displayInfo.mLowTemperature = i;
                if (half == null) {
                    i = 0;
                } else {
                    i = WeatherUtils.getWeatherStatusByWeatherIcon(half.mWeatherIcon);
                }
                displayInfo.mStatus = i;
                return displayInfo;
            }
        }
        return null;
    }
}
