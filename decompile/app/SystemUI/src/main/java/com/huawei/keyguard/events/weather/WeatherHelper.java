package com.huawei.keyguard.events.weather;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.HwUnlockUtils;
import com.huawei.keyguard.util.OsUtils;
import java.util.Calendar;

public class WeatherHelper {
    private static WeatherHelper sInstance;
    private Context mContext = GlobalContext.getContext();
    private LocationInfo mLocationInfo;

    public static synchronized WeatherHelper getInstance() {
        WeatherHelper weatherHelper;
        synchronized (WeatherHelper.class) {
            if (sInstance == null) {
                sInstance = new WeatherHelper();
            }
            weatherHelper = sInstance;
        }
        return weatherHelper;
    }

    private WeatherHelper() {
    }

    public LocationInfo getLocationInfo() {
        return this.mLocationInfo;
    }

    public void setLocationInfo(LocationInfo mLocationInfo) {
        this.mLocationInfo = mLocationInfo;
    }

    public WeatherInfo queryWeatherInfo(int cityType) {
        Bundle queryBundle = new Bundle();
        queryBundle.putInt("city_type", cityType);
        queryBundle.putInt("dayAndNightWeather", 1);
        WeatherInfo weatherInfo = null;
        try {
            weatherInfo = WeatherUtils.parseWeatherInfoBundle(this.mContext.getContentResolver().call(OsUtils.getUserUri("com.huawei.android.weather"), "query_home_city_weather", null, queryBundle));
        } catch (Exception e) {
            HwLog.w("WeatherHelper", "queryHomeCityWeatherIcon exception: " + e.getMessage());
        }
        return weatherInfo;
    }

    public LocationInfo queryLocation() {
        LocationInfo locationInfo = new LocationInfo();
        locationInfo.setCurrentWeatherInfo(queryWeatherInfo(10));
        locationInfo.setHomeWeatherInfo(queryWeatherInfo(15));
        HwLog.d("WeatherHelper", "homeWeatherInfo = " + locationInfo.getHomeWeatherInfo());
        this.mLocationInfo = locationInfo;
        return locationInfo;
    }

    public boolean isShowOneClock() {
        boolean z = true;
        if (!HwUnlockUtils.isDualClockEnabled(GlobalContext.getContext())) {
            HwLog.d("WeatherHelper", "Dualclock not enabled");
            return true;
        } else if (this.mLocationInfo == null) {
            HwLog.d("WeatherHelper", "no location info");
            return true;
        } else if (this.mLocationInfo.getCurrentWeatherInfo() == null || this.mLocationInfo.getHomeWeatherInfo() == null) {
            HwLog.d("WeatherHelper", "WeatherInfo not get");
            return true;
        } else if (TextUtils.isEmpty(this.mLocationInfo.getHomeWeatherInfo().getCityName())) {
            HwLog.d("WeatherHelper", "City info empty");
            return true;
        } else {
            if (Calendar.getInstance().getTimeZone().getRawOffset() != this.mLocationInfo.getHomeWeatherInfo().getTimeZone().getRawOffset()) {
                z = false;
            }
            return z;
        }
    }
}
