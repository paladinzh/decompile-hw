package com.huawei.keyguard.events;

import android.content.Context;
import android.content.Intent;
import android.os.Handler.Callback;
import android.os.Message;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.events.MonitorImpl.MonitorChangeListener;
import com.huawei.keyguard.events.weather.WeatherHelper;

public class WeatherLocationMonitor extends WeatherMonitor implements Callback {
    public WeatherLocationMonitor(Context context, MonitorChangeListener callback, int monitorId) {
        super(context, callback, monitorId);
        AppHandler.addListener(this);
    }

    protected void finalize() throws Throwable {
        AppHandler.removeListener(this);
        super.finalize();
    }

    public void register() {
        EventCenter.getInst().listen(17, this.mEventListener);
        startAsyncQuery();
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 2) {
            GlobalContext.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    WeatherLocationMonitor.this.startAsyncQuery();
                }
            });
        }
        return false;
    }

    protected boolean onPreBrocastReceive(Intent intent) {
        String action = intent.getAction();
        if (!"com.huawei.android.action.CITYINFO_CHANGE".equals(action) && !"com.huawei.android.action.CITYINFO_ADD".equals(action) && !"com.huawei.android.action.CITYINFO_DELETE".equals(action) && !"com.huawei.android.action.WIDGET_CHANGE".equals(action)) {
            return super.onPreBrocastReceive(intent);
        }
        startAsyncQuery();
        return true;
    }

    Object onQueryDatabase() {
        return WeatherHelper.getInstance().queryLocation();
    }
}
