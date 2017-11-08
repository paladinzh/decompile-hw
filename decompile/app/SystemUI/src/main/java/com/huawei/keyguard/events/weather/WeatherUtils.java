package com.huawei.keyguard.events.weather;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.systemui.statusbar.policy.HwCustRemoteInputViewImpl;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherUtils {
    protected static WeatherInfo parseWeatherInfoBundle(Bundle bundle) {
        if (bundle == null) {
            HwLog.w("WeatherUtils", "parseWeatherInfoBundle the bundle is null!");
            return null;
        }
        String weatherInfo = bundle.getString("key_home_city_weather", BuildConfig.FLAVOR);
        if (!TextUtils.isEmpty(weatherInfo)) {
            return getWeatherInfoFromJson(weatherInfo);
        }
        HwLog.w("WeatherUtils", "parseWeatherInfoBundle the weatherInfo is empty!");
        return null;
    }

    public static WeatherInfo getWeatherInfoFromJson(String weatherInfoString) {
        int i = 0;
        if (TextUtils.isEmpty(weatherInfoString)) {
            HwLog.w("WeatherUtils", "getWeatherInfoFromJson the weatherInfoString is empty!");
            return null;
        }
        WeatherInfo weatherInfo = new WeatherInfo();
        try {
            JSONObject jsonObject = new JSONObject(weatherInfoString);
            CityInfo cityInfo = new CityInfo();
            JSONObject cityInfoJsonData = jsonObject.getJSONObject("cityinfo");
            cityInfo.mCityName = cityInfoJsonData.optString("city_en_name");
            cityInfo.mCityNativeName = cityInfoJsonData.optString("city_native_name");
            cityInfo.mTimeZone = cityInfoJsonData.optString("time_zone");
            weatherInfo.setSwich(cityInfoJsonData.optBoolean("is_mylocation_visible"));
            weatherInfo.mCityInfo = cityInfo;
            if (jsonObject.optInt("temp_flag") != 0) {
                i = 1;
            }
            weatherInfo.mTempType = i;
            JSONArray jsonArray = jsonObject.getJSONArray("weather");
            weatherInfo.mWeatherDayInfo = getWeatherDayInfo(jsonArray);
            weatherInfo.mCurrentWeatherInfo = getCurrentWeatherInfo(jsonArray);
        } catch (JSONException e) {
            HwLog.e("WeatherUtils", "getWeatherInfoFromJson the JSONException is: " + e);
        }
        return weatherInfo;
    }

    private static SparseArray<WeatherDayInfo> getWeatherDayInfo(JSONArray jsonArray) throws JSONException {
        SparseArray<WeatherDayInfo> listWeatherDayInfo = new SparseArray();
        if (jsonArray == null) {
            return listWeatherDayInfo;
        }
        int dayCount = jsonArray.length();
        if (1 > dayCount || 6 < dayCount) {
            return listWeatherDayInfo;
        }
        for (int i = 0; i < dayCount; i++) {
            JSONObject dayInfoJsonData = jsonArray.getJSONObject(i);
            WeatherDayInfo weatherDayInfo = new WeatherDayInfo();
            weatherDayInfo.mDayIndex = dayInfoJsonData.optInt("day_index");
            if (weatherDayInfo.mDayIndex != 0) {
                weatherDayInfo.mSunRise = dayInfoJsonData.optLong("sunrise_time");
                weatherDayInfo.mSunSet = dayInfoJsonData.optLong("sunset_time");
                weatherDayInfo.mObsDate = dayInfoJsonData.optLong("observation_time");
                weatherDayInfo.mDayTimeInfo = new WeatherHalfDayInfo();
                weatherDayInfo.mDayTimeInfo.mHighTemperature = (float) dayInfoJsonData.optInt("curr_hightemp");
                weatherDayInfo.mDayTimeInfo.mLowTemperature = (float) dayInfoJsonData.optInt("curr_lowtemp");
                weatherDayInfo.mDayTimeInfo.mWeatherIcon = dayInfoJsonData.optInt("weather_icon");
                weatherDayInfo.mNightTimeInfo = new WeatherHalfDayInfo();
                weatherDayInfo.mNightTimeInfo.mHighTemperature = (float) dayInfoJsonData.optInt("night_curr_hightemp");
                weatherDayInfo.mNightTimeInfo.mLowTemperature = (float) dayInfoJsonData.optInt("night_curr_lowtemp");
                weatherDayInfo.mNightTimeInfo.mWeatherIcon = dayInfoJsonData.optInt("night_weather_icon");
                listWeatherDayInfo.put(weatherDayInfo.mDayIndex - 1, weatherDayInfo);
            }
        }
        return listWeatherDayInfo;
    }

    private static CurrentWeatherInfo getCurrentWeatherInfo(JSONArray jsonArray) throws JSONException {
        CurrentWeatherInfo currentInfo = new CurrentWeatherInfo();
        if (jsonArray == null) {
            HwLog.w("WeatherUtils", "getCurrentWeatherInfo jsonArray is null!");
            return currentInfo;
        }
        int dayCount = jsonArray.length();
        if (1 > dayCount || 6 < dayCount) {
            HwLog.w("WeatherUtils", "getCurrentWeatherInfo dayIndex is wrong with dayCount is: " + dayCount);
            return currentInfo;
        }
        for (int i = 0; i < dayCount; i++) {
            JSONObject dayInfoJsonData = jsonArray.getJSONObject(i);
            if (1 == dayInfoJsonData.optInt("day_index")) {
                currentInfo.mObservationTime = dayInfoJsonData.optLong("observation_time");
                long sunRiseTime = dayInfoJsonData.optLong("sunrise_time");
                long sunSetTime = dayInfoJsonData.optLong("sunset_time");
                long ralativeTime = System.currentTimeMillis();
                if (ralativeTime < sunRiseTime || ralativeTime > sunSetTime) {
                    currentInfo.mTemperature = (float) dayInfoJsonData.optInt("night_current_temperature");
                    currentInfo.mWeatherIcon = dayInfoJsonData.optInt("night_weather_icon");
                } else {
                    currentInfo.mTemperature = (float) dayInfoJsonData.optInt("current_temperature");
                    currentInfo.mWeatherIcon = dayInfoJsonData.optInt("weather_icon");
                }
                HwLog.i("WeatherUtils", "getCurrentWeatherInfo currentTemperature is :" + currentInfo.mTemperature + " mWeatherIcon Id is: " + currentInfo.mWeatherIcon);
                return currentInfo;
            }
        }
        HwLog.i("WeatherUtils", "getCurrentWeatherInfo currentTemperature is :" + currentInfo.mTemperature + " mWeatherIcon Id is: " + currentInfo.mWeatherIcon);
        return currentInfo;
    }

    public static int getRelativeDay(long currentTime, long secondTime, TimeZone timeZone) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeZone(timeZone);
        currentCalendar.setTimeInMillis(currentTime);
        currentCalendar.set(currentCalendar.get(1), currentCalendar.get(2), currentCalendar.get(5), 0, 0, 0);
        Calendar secondCalendar = Calendar.getInstance();
        secondCalendar.setTimeZone(timeZone);
        secondCalendar.setTimeInMillis(secondTime);
        secondCalendar.set(secondCalendar.get(1), secondCalendar.get(2), secondCalendar.get(5), 0, 0, 0);
        return (int) (((currentCalendar.getTimeInMillis() / 1000) - (secondCalendar.getTimeInMillis() / 1000)) / 86400);
    }

    public static int getTemperture(float temperture, int tempType) {
        if (temperture >= 0.0f) {
            return (int) (temperture + 0.5f);
        }
        return (int) (temperture - 0.5f);
    }

    public static int getWeatherStatusByWeatherIcon(int iconIndex) {
        switch (iconIndex) {
            case 0:
            case 1:
            case 9:
            case 10:
            case 27:
            case 28:
            case 33:
                return 9;
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
            case 34:
            case 35:
            case 36:
            case 38:
                return 5;
            case 5:
            case 37:
            case HwCustRemoteInputViewImpl.NUM_PER_GROUP /*70*/:
            case 71:
            case 72:
                return 13;
            case 8:
            case 75:
                return 1;
            case 11:
            case 68:
            case 69:
            case 73:
            case 74:
                return 3;
            case 12:
            case 13:
            case 14:
            case 39:
            case 40:
                return 7;
            case 15:
            case 16:
            case 17:
            case 41:
            case 42:
            case 45:
                return 10;
            case 18:
            case 26:
            case 47:
            case 57:
                return 6;
            case 19:
            case 20:
            case 21:
            case 25:
            case 43:
            case 52:
            case 76:
                return 19;
            case 22:
            case 23:
            case 24:
            case 29:
            case 44:
            case 53:
            case 62:
                return 8;
            case 30:
                return 4;
            case 31:
                return 2;
            case 32:
                return 11;
            case 46:
                return 14;
            case 48:
                return 16;
            case 49:
            case 50:
            case 51:
            case 59:
            case 60:
            case 61:
                return 18;
            case 54:
            case 63:
                return 21;
            case 55:
            case 64:
                return 22;
            case 56:
            case 65:
            case 66:
            case 67:
                return 12;
            case 58:
                return 17;
            default:
                return 0;
        }
    }
}
