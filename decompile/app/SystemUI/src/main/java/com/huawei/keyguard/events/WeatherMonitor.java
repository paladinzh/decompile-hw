package com.huawei.keyguard.events;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Handler;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.events.MonitorImpl.MonitorChangeListener;
import com.huawei.keyguard.events.weather.WeatherHelper;
import com.huawei.keyguard.events.weather.WeatherInfo;
import com.huawei.keyguard.util.HwLog;

public class WeatherMonitor extends MonitorImpl {
    protected IEventListener mEventListener = new IEventListener() {
        public boolean onReceive(Context context, Intent intent) {
            return WeatherMonitor.this.onPreBrocastReceive(intent);
        }
    };
    private Handler mHandler = new Handler();
    protected Object mLock = new Object();
    private WeatherInfo mWeatherInfo;

    public void register() {
        EventCenter.getInst().listen(17, this.mEventListener);
        startAsyncQuery();
    }

    public void unRegister() {
        super.unRegister();
        EventCenter.getInst().stopListen(this.mEventListener);
    }

    public WeatherMonitor(Context context, MonitorChangeListener callback, int monitorId) {
        super(context, callback, monitorId);
    }

    protected boolean onPreBrocastReceive(Intent intent) {
        boolean z = true;
        String action = intent.getAction();
        if ("android.intent.action.TIME_TICK".equals(action)) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    if (WeatherMonitor.this.mWeatherInfo != null) {
                        WeatherInfo info;
                        synchronized (WeatherMonitor.this.mLock) {
                            info = WeatherMonitor.this.mWeatherInfo.copy();
                        }
                        WeatherMonitor.this.mCallback.onMonitorChanged(WeatherMonitor.this.mMonitorId, info);
                    }
                }
            });
            return false;
        } else if ("com.huawei.android.action.WEATHER_CHANGE".equals(action)) {
            int status = intent.getIntExtra("status", 0);
            if (1 == status) {
                startAsyncQuery();
            }
            if (1 != status) {
                z = false;
            }
            return z;
        } else if ("com.huawei.android.action.TEMPERATURE_FORMAT_CHANGE".equals(action)) {
            startAsyncQuery();
            return true;
        } else {
            if ("android.intent.action.DATE_CHANGED".equals(action) && this.mWeatherInfo != null) {
                WeatherInfo info;
                synchronized (this.mLock) {
                    info = this.mWeatherInfo.copy();
                }
                this.mCallback.onMonitorChanged(this.mMonitorId, info);
            }
            return false;
        }
    }

    Object onQueryDatabase() {
        WeatherInfo info = WeatherHelper.getInstance().queryWeatherInfo(10);
        synchronized (this.mLock) {
            this.mWeatherInfo = info;
        }
        return info;
    }

    public static int getWeatherIconId(Context context, int resId, int status) {
        if (context == null) {
            HwLog.e("WeatherMonitor", "getWeatherIconId context is null");
            return 0;
        }
        int iconId = 0;
        if (resId > 0) {
            TypedArray ar = context.getResources().obtainTypedArray(resId);
            if (status < 0 || status >= ar.length()) {
                iconId = ar.getResourceId(0, 0);
            } else {
                iconId = ar.getResourceId(status, 0);
            }
            ar.recycle();
        }
        return iconId;
    }
}
