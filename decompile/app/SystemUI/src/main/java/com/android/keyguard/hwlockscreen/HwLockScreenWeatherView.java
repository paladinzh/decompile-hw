package com.android.keyguard.hwlockscreen;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.keyguard.R$array;
import com.android.keyguard.R$id;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.WeatherMonitor;
import com.huawei.keyguard.events.weather.DispalyWeatherInfo;
import com.huawei.keyguard.events.weather.WeatherInfo;
import com.huawei.keyguard.util.HwLog;

public class HwLockScreenWeatherView extends RelativeLayout {
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            HwLog.i("HwLockScreenWeatherView", "handler the message: " + msg.what);
            switch (msg.what) {
                case 0:
                    HwLockScreenWeatherView.this.refreshWeatherInfo(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    };
    HwUpdateCallback mUpdateCallback = new HwUpdateCallback() {
        public void onWeatherChange(WeatherInfo info) {
            if (info == null) {
                HwLog.w("HwLockScreenWeatherView", "onWeatherChange : weatherInfo is null");
                info = new WeatherInfo();
            }
            DispalyWeatherInfo displayInfo = info.getDispalyWeatherInfo(System.currentTimeMillis(), 0);
            if (displayInfo == null) {
                HwLog.w("HwLockScreenWeatherView", "onWeatherChange : displayInfo is null!");
                return;
            }
            int resId;
            boolean isNight = displayInfo.isNight(System.currentTimeMillis(), 0);
            int status = info.getCurrentWeatherStatus();
            if (isNight) {
                resId = R$array.lockscreen_weather_icon_night;
            } else {
                resId = R$array.lockscreen_weather_icon;
            }
            int weatherIconId = WeatherMonitor.getWeatherIconId(HwLockScreenWeatherView.this.getContext(), resId, status);
            Message msg = Message.obtain();
            msg.arg1 = weatherIconId;
            msg.what = 0;
            HwLockScreenWeatherView.this.mHandler.sendMessage(msg);
        }
    };
    private ImageView mWeatherIcon;

    public HwLockScreenWeatherView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = findViewById(R$id.lockscreen_weather_icon);
        if (view instanceof ImageView) {
            this.mWeatherIcon = (ImageView) view;
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mUpdateCallback);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mUpdateCallback);
    }

    private void refreshWeatherInfo(int weatherIconId) {
        setWeatherIcon(weatherIconId);
    }

    private void setWeatherIcon(int sourceId) {
        if (this.mWeatherIcon != null) {
            this.mWeatherIcon.setImageResource(sourceId);
        }
    }
}
