package com.huawei.watermark.manager.parse.util;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.util.Log;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.manager.parse.util.ParseJson.WeatherDayDataInfo;
import com.huawei.watermark.manager.parse.util.WMWeatherHelper.WeatherDateReceiveListener;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WMWeatherService {
    private static final Uri CONTENT_URI_WEATHER = Uri.parse("content://com.huawei.android.weather");
    public static final int TEMPERATURE_UNIT_C = 0;
    public static final int TEMPERATURE_UNIT_F = 1;
    public static final int TEMPERATURE_UNKOWN = -10000;
    private boolean mCanStart;
    private Context mContext;
    private double mLatitude = -100000.0d;
    private double mLongitude = -100000.0d;
    private boolean mNeedRequestWeatherAfterLocationSussess = false;
    private WMWeatherHelper mWMWeatherHelper;
    private WeatherData mWeatherData;
    private WeatherDateReceiveListener mWeatherDataReceiveListener = new WeatherDateReceiveListener() {
        public void onWeatherDateReceived(String jsonData) {
            Log.d("WeatherService", "onWeatherDateReceived");
            WMWeatherService.this.mWeatherJson = jsonData;
            if (WMWeatherService.this.mNeedRequestWeatherAfterLocationSussess) {
                WMWeatherService.this.handlerWeatherData(jsonData);
                WMWeatherService.this.mNeedRequestWeatherAfterLocationSussess = false;
            }
        }
    };
    private String[] mWeatherIcons = null;
    private String mWeatherJson;
    private List<WeatherUpdateCallback> mWeatherUpdateCallbacks;
    private WeatherUpdateThread mWeatherUpdateThread;

    public interface WeatherUpdateCallback {
        void onWeatherReport(WeatherData weatherData);
    }

    public static final class WeatherData {
        private String mPDescText;
        private int mPNum;
        private String mPStatusText;
        private int mTempUnit;
        private int mTemperature;
        private int mTemperatureHigh;
        private int mTemperatureLow;
        private String mWeatherDes;
        public String mWeatherIcon;
        public int mWeatherId;
        private String mWeatherWindDirection;
        public int mWeatherWindDirectionId;
        private String mWeatherWindPower;
        public int mWeatherWindPowerId;

        public void translateWeatherTextData(Context context) {
            Bundle paramBundle = new Bundle();
            try {
                paramBundle.putInt("wind_speed_text_id", this.mWeatherWindPowerId);
            } catch (Exception e) {
                WMLog.d("WMWeatherService", "WMWeatherService translateWeatherTextData WIND_SPEED_TEXT_ID e=" + e);
            }
            try {
                paramBundle.putInt("wind_direction_text_id", this.mWeatherWindDirectionId);
            } catch (Exception e2) {
                WMLog.d("WMWeatherService", "WMWeatherService translateWeatherTextData WIND_DIRECTION_TEXT_ID e=" + e2);
            }
            try {
                paramBundle.putInt("p_status_tex_p_num", this.mPNum);
            } catch (Exception e22) {
                WMLog.d("WMWeatherService", "WMWeatherService translateWeatherTextData P_STATUS_TEX_P_NUM e=" + e22);
            }
            try {
                paramBundle.putInt("p_desc_tex_p_num", this.mPNum);
            } catch (Exception e222) {
                WMLog.d("WMWeatherService", "WMWeatherService translateWeatherTextData P_DESC_TEXT_P_NUM e=" + e222);
            }
            try {
                paramBundle.putInt("weather_text_id", this.mWeatherId);
            } catch (Exception e2222) {
                WMLog.d("WMWeatherService", "WMWeatherService translateWeatherTextData WEATHER_TEXT_ID e=" + e2222);
            }
            Bundle resuleBundle = context.getContentResolver().call(Uri.parse("content://com.huawei.android.weather"), "query_weather_text", null, paramBundle);
            if (resuleBundle != null) {
                for (String key : resuleBundle.keySet()) {
                    if ("wind_speed_text".equalsIgnoreCase(key)) {
                        this.mWeatherWindPower = resuleBundle.getString(key);
                        Log.d("WeatherService", "mWeatherWindPower value is " + this.mWeatherWindPower + " after query_weather_text");
                    } else if ("wind_direction_text".equalsIgnoreCase(key)) {
                        this.mWeatherWindDirection = resuleBundle.getString(key);
                        Log.d("WeatherService", "mWeatherWindDirection value is " + this.mWeatherWindDirection + " after query_weather_text");
                    } else if ("p_status_text".equalsIgnoreCase(key)) {
                        this.mPStatusText = resuleBundle.getString(key);
                    } else if ("p_desc_text".equalsIgnoreCase(key)) {
                        this.mPDescText = resuleBundle.getString(key);
                    }
                }
                return;
            }
            WMLog.d("WeatherService", "weather version wrong , Bundle resule is null");
        }

        public int getTemperature() {
            return getFTemperatureIfNeed(this.mTemperature);
        }

        public int getTemperatureLow() {
            return getFTemperatureIfNeed(this.mTemperatureLow);
        }

        public int getTemperatureHigh() {
            return getFTemperatureIfNeed(this.mTemperatureHigh);
        }

        public int getTempUnit() {
            return this.mTempUnit;
        }

        public String getWeatherDes() {
            return this.mWeatherDes;
        }

        public String getWeatherIcon() {
            return this.mWeatherIcon;
        }

        public String getWeatherWindDirection() {
            return this.mWeatherWindDirection;
        }

        public String getWeatherWindPower() {
            return this.mWeatherWindPower;
        }

        public String getPStatusText() {
            return this.mPStatusText;
        }

        public String getPDescText() {
            return this.mPDescText;
        }

        private int getFTemperatureIfNeed(int temperature) {
            return temperature;
        }
    }

    public static final class WeatherInfo {
        public long mObservationTime;
        public int mPNum;
        public int mTempUnit;
        public float mTemperature;
        public float mTemperatureHigh;
        public float mTemperatureLow;
        public String mWeatherDes;
        public int mWeatherIcon;
        public int mWindDirection;
        public int mWindSpeed;
    }

    private class WeatherUpdateThread extends Thread {
        private boolean mCancel;
        private ConditionVariable mSig = new ConditionVariable();

        public WeatherUpdateThread() {
            setName("WeatherUpdateThread");
        }

        public void run() {
            WeatherInfo weatherInfo = WMWeatherService.this.getWeatherInfo(WMWeatherService.this.mContext);
            if (weatherInfo == null && !this.mCancel) {
                this.mSig.close();
                this.mSig.block(1000);
                if (!isContextIsNullOrCancel()) {
                    weatherInfo = WMWeatherService.this.getWeatherInfo(WMWeatherService.this.mContext);
                } else {
                    return;
                }
            }
            while (weatherInfo == null && !this.mCancel) {
                this.mSig.close();
                this.mSig.block(1000);
                if (!isContextIsNullOrCancel()) {
                    weatherInfo = WMWeatherService.this.getWeatherInfo(WMWeatherService.this.mContext);
                } else {
                    return;
                }
            }
            if (weatherInfo != null && weatherInfo.mWeatherIcon == -1) {
                WMWeatherService.this.getNeedRequestWeatherAfterLocationSussess();
            } else if (!this.mCancel) {
                WMWeatherService.this.mWeatherData = WMWeatherService.this.genWeatherData(weatherInfo, WMWeatherService.this.mContext);
                if (WMWeatherService.this.mWeatherData != null) {
                    Log.d("WeatherService", "WMWeatherService WeatherUpdateThread mWeatherData.mTemperature=" + WMWeatherService.this.mWeatherData.mTemperature + ";mWeatherData.mTemperatureLow=" + WMWeatherService.this.mWeatherData.mTemperatureLow + ";mWeatherData.mTemperatureHigh=" + WMWeatherService.this.mWeatherData.mTemperatureHigh + ";mWeatherData.mWeatherIcon=" + WMWeatherService.this.mWeatherData.mWeatherIcon + ";mWeatherData.mWeatherWindPower" + WMWeatherService.this.mWeatherData.mWeatherWindPower + ";mWeatherData.mWeatherWindDirection" + WMWeatherService.this.mWeatherData.mWeatherWindDirection);
                }
                if (!notUpdateWeather()) {
                    Log.d("WeatherService", "update weather data to callbacks!!!");
                    for (WeatherUpdateCallback callbak : WMWeatherService.this.mWeatherUpdateCallbacks) {
                        callbak.onWeatherReport(WMWeatherService.this.mWeatherData);
                    }
                    WMWeatherService.this.mWeatherUpdateThread = null;
                }
            }
        }

        private boolean isContextIsNullOrCancel() {
            return WMWeatherService.this.mContext != null ? this.mCancel : true;
        }

        private boolean notUpdateWeather() {
            return (this.mCancel || WMWeatherService.this.mWeatherData == null) ? true : WMCollectionUtil.isEmptyCollection(WMWeatherService.this.mWeatherUpdateCallbacks);
        }

        public void cancel() {
            this.mCancel = true;
            this.mSig.open();
        }
    }

    public WMWeatherService(Context context, WMWeatherHelper wmWeatherHelper) {
        this.mContext = context;
        this.mWeatherIcons = this.mContext.getResources().getStringArray(WMResourceUtil.getArrayId(context, "water_mark_weather_icons"));
        this.mWMWeatherHelper = wmWeatherHelper;
    }

    public void start() {
        this.mCanStart = true;
        startWeatherUpdateThread();
        this.mWMWeatherHelper.start();
        this.mWMWeatherHelper.setWeatherDateReceiveListener(this.mWeatherDataReceiveListener);
    }

    public void release() {
        this.mCanStart = false;
        if (this.mWeatherUpdateThread != null) {
            this.mWeatherUpdateThread.cancel();
        }
        this.mWeatherUpdateCallbacks = null;
        this.mWeatherUpdateThread = null;
        this.mWeatherData = null;
        this.mWeatherJson = null;
        this.mWMWeatherHelper.release();
        this.mWMWeatherHelper.setWeatherDateReceiveListener(null);
    }

    private void getNeedRequestWeatherAfterLocationSussess() {
        if (this.mWeatherJson == null) {
            Log.d("WeatherService", "wait till location is obtained");
            this.mNeedRequestWeatherAfterLocationSussess = true;
            return;
        }
        Log.d("WeatherService", "weather data is obtained by location");
        handlerWeatherData(this.mWeatherJson);
        this.mNeedRequestWeatherAfterLocationSussess = false;
    }

    public void addWeatherUpdateCallback(WeatherUpdateCallback weatherUpdateCallback) {
        if (this.mWeatherUpdateCallbacks == null) {
            this.mWeatherUpdateCallbacks = new ArrayList();
        }
        this.mWeatherUpdateCallbacks.add(weatherUpdateCallback);
        if (this.mWeatherData != null) {
            weatherUpdateCallback.onWeatherReport(this.mWeatherData);
        }
        startWeatherUpdateThread();
    }

    private void startWeatherUpdateThread() {
        if (this.mWeatherUpdateThread == null && this.mCanStart) {
            this.mWeatherUpdateThread = new WeatherUpdateThread();
            this.mWeatherUpdateThread.start();
        }
    }

    private WeatherInfo getWeatherInfo(Context context) {
        if (context == null) {
            return null;
        }
        return getWeatherFromDB(context);
    }

    private WeatherInfo parseWeatherBundleJsonData(Context context, Bundle bundleData) {
        WeatherInfo homeCityWeatherinfo = new WeatherInfo();
        if (bundleData != null) {
            String jsonData = bundleData.getString("key_home_city_weather");
            if (jsonData != null) {
                return parseWeatherData(jsonData, homeCityWeatherinfo);
            }
        }
        Log.d("WeatherService", "no weather data, parseWeatherBundleJsonData over !");
        return homeCityWeatherinfo;
    }

    private WeatherInfo parseWeatherData(String jsonData, WeatherInfo weatherInfo) {
        WeatherInfo homeCityWeatherinfo = weatherInfo;
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            ParseJson parseJson = new ParseJson();
            parseJson.mTempFlag = jsonObject.optInt(ParseJson.TEMP_FLAG);
            parseJson.mTempUnit = jsonObject.optString(ParseJson.TEMP_UNIT);
            JSONArray jsonArray = jsonObject.optJSONArray(ParseJson.KEY_WEATHER);
            if (jsonArray == null) {
                Log.e("WeatherService", "get ParseJson.KEY_WEATHER array is null!!");
                return null;
            } else if (jsonArray.length() <= 1) {
                Log.d("WeatherService", "get home city weather failed!!");
                weatherInfo.mWeatherIcon = -1;
                return weatherInfo;
            } else {
                int dayCount = jsonArray.length();
                WeatherDayDataInfo dayDataInfo = new WeatherDayDataInfo();
                int i = 0;
                while (i < dayCount) {
                    try {
                        JSONObject dayInfoJsonData = jsonArray.getJSONObject(i);
                        int dayIndex = dayInfoJsonData.optInt(ParseJson.DAY_INDEX);
                        dayDataInfo.mDayIndex = dayIndex;
                        dayDataInfo.mWeatherObsDate = dayInfoJsonData.optLong(ParseJson.OBSERVATION_TIME);
                        dayDataInfo.mSunriseTime = dayInfoJsonData.optLong(ParseJson.SUNRISE_TIME);
                        dayDataInfo.mSunsetTime = dayInfoJsonData.optLong(ParseJson.SUNSET_TIME);
                        long time = System.currentTimeMillis();
                        if (!dayInfoJsonData.has(ParseJson.NIGHT_WEATHER_ICON) || (time > dayDataInfo.mSunriseTime && time < dayDataInfo.mSunsetTime)) {
                            dayDataInfo.mWeatherIcon = dayInfoJsonData.optInt(ParseJson.WEATHER_ICON);
                            dayDataInfo.mWeatherDes = dayInfoJsonData.optString(ParseJson.WEATHER_NATIVE_DES);
                            dayDataInfo.mTemperature = dayInfoJsonData.optInt(ParseJson.CURRENT_TEMP);
                            dayDataInfo.mHighTemperature = dayInfoJsonData.optInt(ParseJson.CURR_HIGHTEMP);
                            dayDataInfo.mLowTemperature = dayInfoJsonData.optInt(ParseJson.CURR_LOWTEMP);
                        } else {
                            dayDataInfo.mWeatherIcon = dayInfoJsonData.optInt(ParseJson.NIGHT_WEATHER_ICON);
                            dayDataInfo.mWeatherDes = dayInfoJsonData.optString(ParseJson.NIGHT_WEATHER_NATIVE_DES);
                            dayDataInfo.mTemperature = dayInfoJsonData.optInt(ParseJson.NIGHT_CURRENT_TEMP);
                            dayDataInfo.mHighTemperature = dayInfoJsonData.optInt(ParseJson.NIGHT_CURR_HIGHTEMP);
                            dayDataInfo.mLowTemperature = dayInfoJsonData.optInt(ParseJson.NIGHT_CURR_LOWTEMP);
                        }
                        dayDataInfo.mNightWeatherIcon = dayInfoJsonData.optInt(ParseJson.NIGHT_WEATHER_ICON);
                        dayDataInfo.mNightWeatherDes = dayInfoJsonData.optString(ParseJson.NIGHT_WEATHER_NATIVE_DES);
                        dayDataInfo.mNightTemperature = dayInfoJsonData.optInt(ParseJson.NIGHT_CURRENT_TEMP);
                        dayDataInfo.mNightHighTemp = dayInfoJsonData.optInt(ParseJson.NIGHT_CURR_HIGHTEMP);
                        dayDataInfo.mNightLowTemp = dayInfoJsonData.optInt(ParseJson.NIGHT_CURR_LOWTEMP);
                        dayDataInfo.mAirQuality = dayInfoJsonData.optString(ParseJson.AIR_QUALITY);
                        dayDataInfo.mAirPnum = dayInfoJsonData.optInt(ParseJson.AIR_PNUM, -1);
                        dayDataInfo.mAirPM25 = dayInfoJsonData.optInt(ParseJson.AIR_PM25, -1);
                        dayDataInfo.mAirPM10 = dayInfoJsonData.optInt(ParseJson.AIR_PM10, -1);
                        dayDataInfo.mAirStatusDesc = dayInfoJsonData.optString(ParseJson.AIR_STATUS_DESC);
                        dayDataInfo.mWindSpeed = dayInfoJsonData.optInt(ParseJson.WIND_SPEED);
                        dayDataInfo.mWindDirection = dayInfoJsonData.optInt(ParseJson.WIND_DIRECTION);
                        parseJson.mForecastDayInfo.put(dayIndex, dayDataInfo);
                        if (getRelativeDay(System.currentTimeMillis(), dayDataInfo.mWeatherObsDate) == 0) {
                            break;
                        }
                        i++;
                    } catch (JSONException e) {
                        Log.e("WeatherService", "get json object failed");
                        return null;
                    }
                }
                weatherInfo.mObservationTime = dayDataInfo.mWeatherObsDate;
                weatherInfo.mTemperature = (float) dayDataInfo.mTemperature;
                weatherInfo.mTemperatureHigh = (float) dayDataInfo.mHighTemperature;
                weatherInfo.mTemperatureLow = (float) dayDataInfo.mLowTemperature;
                weatherInfo.mWeatherIcon = dayDataInfo.mWeatherIcon;
                weatherInfo.mWindSpeed = dayDataInfo.mWindSpeed;
                weatherInfo.mWindDirection = dayDataInfo.mWindDirection;
                weatherInfo.mPNum = dayDataInfo.mAirPnum;
                weatherInfo.mWeatherDes = dayDataInfo.mWeatherDes;
                Log.d("WeatherService", "mObservationTime:" + weatherInfo.mObservationTime + " mTemperature:" + weatherInfo.mTemperature + " mWeatherIcon:" + weatherInfo.mWeatherIcon + " mWindSpeed:" + weatherInfo.mWindSpeed + " mWindDirection:" + weatherInfo.mWindDirection + " mPNum:" + weatherInfo.mPNum + " mWeatherDes:" + weatherInfo.mWeatherDes);
                weatherInfo.mTempUnit = jsonObject.optInt(ParseJson.TEMP_FLAG);
                weatherInfo.mPNum = jsonObject.optInt(ParseJson.AIR_PNUM);
                return weatherInfo;
            }
        } catch (JSONException e2) {
            Log.e("WeatherService", "parse weather data failed");
            return null;
        }
    }

    private WeatherInfo getWeatherFromDB(Context context) {
        WeatherInfo weatherInfo = null;
        try {
            Log.d("WeatherService", "begin to query data from weather!!!");
            Bundle inputBundle = new Bundle();
            inputBundle.putInt("city_type", 10);
            weatherInfo = parseWeatherBundleJsonData(context, context.getContentResolver().call(CONTENT_URI_WEATHER, "query_home_city_weather", null, inputBundle));
        } catch (Exception e) {
            Log.e("WeatherService", "queryHomeCityWeatherIcon exception >> " + e.getMessage());
        }
        if (weatherInfo == null) {
            return null;
        }
        if (getRelativeDay(System.currentTimeMillis(), weatherInfo.mObservationTime) <= 5 || weatherInfo.mWeatherIcon == -1) {
            return weatherInfo;
        }
        Log.e("WeatherService", "weather data out of date!!!");
        return null;
    }

    private int getRelativeDay(long currentTime, long secondTime) {
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(currentTime);
        currentCalendar.set(currentCalendar.get(1), currentCalendar.get(2), currentCalendar.get(5), 0, 0, 0);
        Calendar secondCalendar = Calendar.getInstance();
        secondCalendar.setTimeInMillis(secondTime);
        secondCalendar.set(secondCalendar.get(1), secondCalendar.get(2), secondCalendar.get(5), 0, 0, 0);
        return (int) (((currentCalendar.getTimeInMillis() / 1000) - (secondCalendar.getTimeInMillis() / 1000)) / 86400);
    }

    private WeatherData genWeatherData(WeatherInfo info, Context context) {
        if (info == null) {
            return null;
        }
        WeatherData weatherData = new WeatherData();
        Log.e("WeatherService", "mWeatherIcons.length:" + this.mWeatherIcons.length);
        int weatherIcon = info.mWeatherIcon;
        if (weatherIcon <= 0 || weatherIcon >= this.mWeatherIcons.length) {
            weatherData.mTemperature = TEMPERATURE_UNKOWN;
            weatherData.mTemperatureLow = TEMPERATURE_UNKOWN;
            weatherData.mTemperatureHigh = TEMPERATURE_UNKOWN;
            weatherData.mWeatherIcon = this.mWeatherIcons[0];
        } else {
            weatherData.mTemperature = Math.round(info.mTemperature);
            weatherData.mTemperatureLow = Math.round(info.mTemperatureLow);
            weatherData.mTemperatureHigh = Math.round(info.mTemperatureHigh);
            weatherData.mWeatherIcon = this.mWeatherIcons[weatherIcon];
        }
        weatherData.mWeatherId = info.mWeatherIcon;
        weatherData.mWeatherWindPowerId = info.mWindSpeed;
        weatherData.mWeatherWindDirectionId = info.mWindDirection;
        weatherData.mPNum = info.mPNum;
        weatherData.mTempUnit = info.mTempUnit;
        weatherData.mWeatherDes = info.mWeatherDes;
        weatherData.translateWeatherTextData(context);
        return weatherData;
    }

    public void handlerWeatherData(String jsonData) {
        WeatherInfo weatherInfo = parseWeatherData(jsonData, new WeatherInfo());
        if (weatherInfo != null) {
            if (getRelativeDay(System.currentTimeMillis(), weatherInfo.mObservationTime) > 5) {
                Log.e("WeatherService", "weather data out of date!!!");
                return;
            }
            this.mWeatherData = genWeatherData(weatherInfo, this.mContext);
            if (this.mWeatherData != null) {
                Log.d("WeatherService", "WMWeatherService WeatherUpdateThread mWeatherData.mTemperature=" + this.mWeatherData.mTemperature + ";mWeatherData.mTemperatureLow=" + this.mWeatherData.mTemperatureLow + ";mWeatherData.mTemperatureHigh=" + this.mWeatherData.mTemperatureHigh + ";mWeatherData.mWeatherIcon=" + this.mWeatherData.mWeatherIcon + ";mWeatherData.mWeatherWindPower" + this.mWeatherData.mWeatherWindPower + ";mWeatherData.mWeatherWindDirection" + this.mWeatherData.mWeatherWindDirection);
            }
            if (this.mCanStart && this.mWeatherData != null && !WMCollectionUtil.isEmptyCollection(this.mWeatherUpdateCallbacks)) {
                Log.d("WeatherService", "update weather data to callbacks!!!");
                for (WeatherUpdateCallback callbak : this.mWeatherUpdateCallbacks) {
                    callbak.onWeatherReport(this.mWeatherData);
                }
            }
        }
    }
}
