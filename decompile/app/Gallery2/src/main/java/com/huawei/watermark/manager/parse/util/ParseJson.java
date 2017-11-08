package com.huawei.watermark.manager.parse.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParseJson {
    public static final String AIR_PM10 = "air_pm10";
    public static final String AIR_PM25 = "air_pm25";
    public static final String AIR_PNUM = "air_pnum";
    public static final String AIR_QUALITY = "air_quality";
    public static final String AIR_STATUS_DESC = "air_status_desc";
    public static final String ALARM_ID = "alarm_id";
    public static final String ALARM_OBSERVATIONTIME = "observationtime";
    public static final String ALARM_TILE = "alarmtitle";
    public static final String ALARM_TYPE = "alarm_type";
    public static final String ALARM_TYPE_NAME = "alarm_type_name";
    public static final String CITY_CODE = "city_code";
    public static final String CITY_NAME = "city_en_name";
    public static final String CITY_NATIVE_NAME = "city_native_name";
    public static final String CITY_TYPE = "city_type";
    public static final String CURRENT_TEMP = "current_temperature";
    public static final String CURR_HIGHTEMP = "curr_hightemp";
    public static final String CURR_LOWTEMP = "curr_lowtemp";
    public static final String DAY_INDEX = "day_index";
    public static final int DEFAULT_AIR_PNUM = -1;
    public static final int DEFAULT_PNUM = -1;
    public static final String KEY_CITYINFO = "cityinfo";
    public static final String KEY_WEATHER = "weather";
    public static final String LEVEL = "level";
    public static final String LEVEL_NAME = "level_name";
    public static final String NIGHT_CURRENT_TEMP = "night_current_temperature";
    public static final String NIGHT_CURR_HIGHTEMP = "night_curr_hightemp";
    public static final String NIGHT_CURR_LOWTEMP = "night_curr_lowtemp";
    public static final String NIGHT_WEATHER_ICON = "night_weather_icon";
    public static final String NIGHT_WEATHER_NATIVE_DES = "night_weather_native_des";
    public static final String OBSERVATION_TIME = "observation_time";
    public static final String PNUM = "pnum";
    public static final String STATE_NAME = "state_name";
    public static final String SUNRISE_TIME = "sunrise_time";
    public static final String SUNSET_TIME = "sunset_time";
    public static final String TAG = "ParseJson_CityWeather";
    public static final String TEMP_FLAG = "temp_flag";
    public static final String TEMP_UNIT = "temp_unit";
    public static final String TIME_ZONE = "time_zone";
    public static final String WEATHER_ICON = "weather_icon";
    public static final String WEATHER_NATIVE_DES = "weather_native_des";
    public static final String WIND_DIRECTION = "wind_direction";
    public static final String WIND_SPEED = "wind_speed";
    public CityInfo mCityInfo = new CityInfo();
    public int mCrruDayIndex = 1;
    public SparseArray<WeatherDayDataInfo> mForecastDayInfo = new SparseArray();
    public boolean mIsTwcVersion = false;
    public int mTempFlag = 0;
    public String mTempUnit = "";
    public SparseArray<WeatherAlarm> mWeatherAlarm = new SparseArray();

    public static final class CityInfo {
        public String mCityCode = "";
        public String mCityName = "";
        public String mCityNativeName = "";
        public int mCityType;
        public String mStateName = "";
        public String mTimeZone = "";

        public String getDisplayName() {
            if (TextUtils.isEmpty(this.mCityNativeName)) {
                return this.mCityName;
            }
            return this.mCityNativeName;
        }

        public String toString() {
            return "CityInfo [mCityName=" + this.mCityName + ", mCityNativeName=" + this.mCityNativeName + ", mStateName=" + this.mStateName + ", mCityCode=" + this.mCityCode + ", mTimeZone=" + this.mTimeZone + ", mCityType=" + this.mCityType + "]";
        }
    }

    public static final class WeatherAlarm {
        public String mAarmId;
        public int mAlarmType;
        public String mAlarmTypeName;
        public String mAlarmTypeTitle;
        public int mLevel;
        public String mLevelName;
        public long mObservationtime;
    }

    public static final class WeatherDayDataInfo {
        public int mAirPM10 = -1;
        public int mAirPM25 = -1;
        public int mAirPnum = -1;
        public String mAirQuality = "";
        public String mAirStatusDesc = "";
        public int mDayIndex;
        public int mHighTemperature;
        public int mLowTemperature;
        public int mNightHighTemp;
        public int mNightLowTemp;
        public int mNightTemperature;
        public String mNightWeatherDes = "";
        public int mNightWeatherIcon;
        public int mPnum = -1;
        public long mSunriseTime;
        public long mSunsetTime;
        public int mTemperature;
        public String mWeatherDes = "";
        public int mWeatherIcon;
        public long mWeatherObsDate;
        public int mWindDirection;
        public int mWindSpeed;

        public String toString() {
            return "DayDataInfo [mDayIndex=" + this.mDayIndex + ", Icon=" + this.mWeatherIcon + ",Des=" + this.mWeatherDes + ",ObsDate=" + this.mWeatherObsDate + ",Temp=" + this.mTemperature + ",highTemp=" + this.mHighTemperature + ",lowTemp=" + this.mLowTemperature + ", Sunrise=" + this.mSunriseTime + ",Sunset=" + this.mSunsetTime + ",mAirQuality=" + this.mAirQuality + ", mAirPnum=" + this.mAirPnum + ",PM25=" + this.mAirPM25 + ",PM10=" + this.mAirPM10 + ", mAirStatus=" + this.mAirStatusDesc + "]";
        }
    }

    public String toString() {
        return "HomeCityWeather [mCityInfo=" + this.mCityInfo + ", mIsTwcVersion=" + this.mIsTwcVersion + ", mTempFlag=" + this.mTempFlag + ", mTempUnit=" + this.mTempUnit + ", mCrruDayIndex=" + this.mCrruDayIndex + ", mForecastDayInfo=" + this.mForecastDayInfo + ", alarm : " + this.mWeatherAlarm + "]";
    }

    public static Calendar getCurCityCalendar(TimeZone timezone) {
        Calendar calendar = Calendar.getInstance();
        if (timezone == null) {
            return calendar;
        }
        long millis = calendar.getTimeInMillis();
        calendar.setTimeInMillis(((long) (timezone.getOffset(millis) - TimeZone.getDefault().getOffset(millis))) + millis);
        return calendar;
    }

    public static ParseJson parseBundleData(Context context, String jsonData) throws JSONException {
        int i;
        Log.e(TAG, "parseBundleData jsonData = " + jsonData);
        JSONObject jsonObject = new JSONObject(jsonData);
        ParseJson parseJson = new ParseJson();
        parseJson.mTempFlag = jsonObject.optInt(TEMP_FLAG);
        parseJson.mTempUnit = jsonObject.optString(TEMP_UNIT);
        JSONObject cityInfoJsonData = jsonObject.getJSONObject(KEY_CITYINFO);
        parseJson.mCityInfo.mCityName = cityInfoJsonData.optString(CITY_NAME);
        parseJson.mCityInfo.mCityNativeName = cityInfoJsonData.optString(CITY_NATIVE_NAME);
        parseJson.mCityInfo.mStateName = cityInfoJsonData.optString(STATE_NAME);
        parseJson.mCityInfo.mCityCode = cityInfoJsonData.optString(CITY_CODE);
        parseJson.mCityInfo.mTimeZone = cityInfoJsonData.optString(TIME_ZONE);
        parseJson.mCityInfo.mCityType = cityInfoJsonData.optInt("city_type");
        JSONArray jsonArray = jsonObject.optJSONArray(KEY_WEATHER);
        int dayCount = jsonArray == null ? 0 : jsonArray.length();
        for (i = 0; i < dayCount; i++) {
            JSONObject dayInfoJsonData = jsonArray.getJSONObject(i);
            WeatherDayDataInfo dayDataInfo = new WeatherDayDataInfo();
            int dayIndex = dayInfoJsonData.optInt(DAY_INDEX);
            dayDataInfo.mDayIndex = dayIndex;
            dayDataInfo.mWeatherObsDate = dayInfoJsonData.optLong(OBSERVATION_TIME);
            dayDataInfo.mSunriseTime = dayInfoJsonData.optLong(SUNRISE_TIME);
            dayDataInfo.mSunsetTime = dayInfoJsonData.optLong(SUNSET_TIME);
            dayDataInfo.mWeatherIcon = dayInfoJsonData.optInt(WEATHER_ICON);
            dayDataInfo.mWeatherDes = dayInfoJsonData.optString(WEATHER_NATIVE_DES);
            dayDataInfo.mTemperature = dayInfoJsonData.optInt(CURRENT_TEMP);
            dayDataInfo.mHighTemperature = dayInfoJsonData.optInt(CURR_HIGHTEMP);
            dayDataInfo.mLowTemperature = dayInfoJsonData.optInt(CURR_LOWTEMP);
            dayDataInfo.mNightWeatherIcon = dayInfoJsonData.optInt(NIGHT_WEATHER_ICON);
            dayDataInfo.mNightWeatherDes = dayInfoJsonData.optString(NIGHT_WEATHER_NATIVE_DES);
            dayDataInfo.mNightTemperature = dayInfoJsonData.optInt(NIGHT_CURRENT_TEMP);
            dayDataInfo.mNightHighTemp = dayInfoJsonData.optInt(NIGHT_CURR_HIGHTEMP);
            dayDataInfo.mNightLowTemp = dayInfoJsonData.optInt(NIGHT_CURR_LOWTEMP);
            dayDataInfo.mAirQuality = dayInfoJsonData.optString(AIR_QUALITY);
            dayDataInfo.mPnum = dayInfoJsonData.optInt(PNUM, -1);
            dayDataInfo.mAirPnum = dayInfoJsonData.optInt(AIR_PNUM, -1);
            dayDataInfo.mAirPM25 = dayInfoJsonData.optInt(AIR_PM25, -1);
            dayDataInfo.mAirPM10 = dayInfoJsonData.optInt(AIR_PM10, -1);
            dayDataInfo.mAirStatusDesc = dayInfoJsonData.optString(AIR_STATUS_DESC);
            parseJson.mForecastDayInfo.put(dayIndex, dayDataInfo);
        }
        JSONArray weatherAlarmJsonArray = jsonObject.optJSONArray("alarm_weather");
        if (weatherAlarmJsonArray == null) {
            Log.e(TAG, "weatherAlarmJsonArray == null");
            return parseJson;
        }
        int alarmCount = weatherAlarmJsonArray.length();
        for (i = 0; i < alarmCount; i++) {
            dayInfoJsonData = weatherAlarmJsonArray.getJSONObject(i);
            WeatherAlarm weatherAlarm = new WeatherAlarm();
            weatherAlarm.mAarmId = dayInfoJsonData.optString(ALARM_ID);
            weatherAlarm.mLevel = dayInfoJsonData.optInt(LEVEL);
            weatherAlarm.mLevelName = dayInfoJsonData.optString(LEVEL_NAME);
            weatherAlarm.mAlarmType = dayInfoJsonData.optInt(ALARM_TYPE);
            weatherAlarm.mAlarmTypeName = dayInfoJsonData.optString(ALARM_TYPE_NAME);
            weatherAlarm.mObservationtime = dayInfoJsonData.optLong(ALARM_OBSERVATIONTIME);
            weatherAlarm.mAlarmTypeTitle = dayInfoJsonData.optString(ALARM_TILE);
            parseJson.mWeatherAlarm.put(i, weatherAlarm);
        }
        Log.e(TAG, "parseBundleData homeCityWeather = " + parseJson);
        return parseJson;
    }
}
