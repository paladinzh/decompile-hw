package com.android.systemui.time;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.Proguard;
import fyusion.vislib.BuildConfig;
import java.util.ArrayList;

public class TimeManager {
    private static final String TAG = TimeManager.class.getSimpleName();
    private static TimeManager sInstance;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (1 == msg.what) {
                TimeManager.this.updateText();
            }
        }
    };
    private boolean mIsRegistered = false;
    private boolean mIsScrrenon = false;
    private TimeBroadcastReceiver mTimeBroadcastReceiver = null;
    private final ArrayList<TimeChangeCallback> mTimeChangeCallbackList = new ArrayList();

    public interface TimeChangeCallback {
        void onTimeChange(long j);
    }

    private class TimeBroadcastReceiver extends BroadcastReceiver {
        private TimeBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            HwLog.i(TimeManager.TAG, BuildConfig.FLAVOR + Proguard.get(intent));
            if ("android.intent.action.SCREEN_ON".equals(action)) {
                TimeManager.this.mIsScrrenon = true;
                TimeManager.this.updateNextData(0);
            } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                TimeManager.this.mIsScrrenon = false;
                TimeManager.this.clearDelayMessage();
            } else {
                TimeManager.this.updateNextData(0);
            }
        }
    }

    public void registerTimeChangeCallback(TimeChangeCallback callback) {
        if (!this.mTimeChangeCallbackList.contains(callback)) {
            this.mTimeChangeCallbackList.add(callback);
        }
    }

    public void unRegisterTimeChangeCallback(TimeChangeCallback callback) {
        this.mTimeChangeCallbackList.remove(callback);
    }

    private TimeManager() {
    }

    public static synchronized TimeManager getInstance() {
        TimeManager timeManager;
        synchronized (TimeManager.class) {
            if (sInstance == null) {
                sInstance = new TimeManager();
            }
            timeManager = sInstance;
        }
        return timeManager;
    }

    public void registerTimeReceiver(Context context) {
        if (!this.mIsRegistered) {
            this.mTimeBroadcastReceiver = new TimeBroadcastReceiver();
            Context context2 = context;
            context2.registerReceiverAsUser(this.mTimeBroadcastReceiver, UserHandle.ALL, getTimeChagesFilter(), null, null);
        }
    }

    private IntentFilter getTimeChagesFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.DATE_CHANGED");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.SCREEN_ON");
        return filter;
    }

    public String getChinaDateTime(Context context, long timeMillis) {
        String chinaDateTime = HwDateUtils.formatChinaDateTime(context, timeMillis, 1);
        HwLog.i(TAG, "chinaDateTime:" + chinaDateTime);
        return chinaDateTime;
    }

    private void updateText() {
        long currentTimeMillis = System.currentTimeMillis();
        for (TimeChangeCallback callback : this.mTimeChangeCallbackList) {
            callback.onTimeChange(System.currentTimeMillis());
        }
        long mills = currentTimeMillis % 60000;
        if (this.mIsScrrenon) {
            updateNextData(60000 - mills);
        }
    }

    private void clearDelayMessage() {
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        }
    }

    private void updateNextData(long millis) {
        clearDelayMessage();
        if (millis > 60000) {
            millis = 60000;
        }
        if (0 >= millis) {
            this.mHandler.sendEmptyMessage(1);
        } else {
            this.mHandler.sendEmptyMessageDelayed(1, millis);
        }
    }
}
