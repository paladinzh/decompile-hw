package com.android.settings;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.util.Log;
import java.util.Calendar;

public class CalculatorModel {
    private static CalculatorModel mInstance = null;
    private AlarmManager mAlarmManager;
    private Intent mCalculaterService;
    private Context mContext;
    private long mLastCount;
    private boolean mLastSavedCountInitialized = false;
    private Notification mNotification;
    private NotificationManager mNotificationManager;
    private Intent mPairingIntent;
    private PendingIntent mPendingIntent;

    private CalculatorModel(Context context) {
        this.mContext = context.getApplicationContext();
        this.mCalculaterService = new Intent().setClassName("com.android.settings", "com.android.settings.CalculatorService");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent("huawei.intent.action.TWO_O_CLOCK"), 0);
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        this.mPairingIntent = new Intent();
        this.mPairingIntent.setAction("huawei.intent.action.CALCULATOR_SETTINGS");
        this.mPairingIntent.setFlags(335544320);
        this.mNotification = new Notification();
        this.mNotification.icon = 2130838685;
        this.mNotification.largeIcon = BitmapFactory.decodeResource(this.mContext.getResources(), 2130838699);
        this.mNotification.defaults = 0;
        this.mNotification.sound = null;
        this.mNotification.vibrate = null;
        this.mNotification.flags = 2;
        this.mNotification.tickerText = this.mContext.getString(2131629224);
    }

    public static CalculatorModel getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CalculatorModel(context);
        }
        return mInstance;
    }

    public void onSensorChanged(long currentCount) {
        Log.d("CalculatorModel", "STEP_COUNTER: " + currentCount);
        if (this.mLastCount != currentCount && getCalculatorEnable()) {
            if (this.mLastCount > currentCount) {
                updateState("last_saved_count", currentCount);
            }
            this.mLastCount = currentCount;
            if (!this.mLastSavedCountInitialized) {
                Log.d("CalculatorModel", "init mLastSavedCountInitialized");
                updateState("last_saved_count", currentCount);
                this.mLastSavedCountInitialized = true;
            }
            updateCount(currentCount);
            showNotification();
        }
    }

    private void showNotification() {
        if (getCalculatorEnable() && getNotificationEnable() && !Utils.isCheckAppExist(this.mContext, "com.huawei.health")) {
            this.mNotification.setLatestEventInfo(this.mContext, this.mContext.getString(2131629223) + ":" + " " + this.mContext.getResources().getQuantityString(2131689542, (int) queryTodayCount(), new Object[]{Integer.valueOf((int) queryTodayCount())}), this.mContext.getString(2131629226) + ":" + " " + this.mContext.getResources().getQuantityString(2131689542, (int) queryYesterdayCount(), new Object[]{Integer.valueOf((int) queryYesterdayCount())}), PendingIntent.getActivityAsUser(this.mContext, 0, this.mPairingIntent, 0, null, UserHandle.CURRENT));
            this.mNotification.tickerText = this.mContext.getString(2131629224);
            this.mNotificationManager.notifyAsUser(null, 2130838699, this.mNotification, UserHandle.ALL);
        }
    }

    private void updateState(String key, long count) {
        StateSaverFactory.getSaver().update(this.mContext, key, count);
    }

    public boolean getNotificationEnable() {
        return 1 == Global.getInt(this.mContext.getContentResolver(), "show_calculator_notification", 1);
    }

    public void setCalculatorEnable(boolean enable) {
        Global.putInt(this.mContext.getContentResolver(), "calculator_enable", enable ? 1 : 0);
        if (enable) {
            showNotification();
        } else {
            this.mNotificationManager.cancel(2130838699);
        }
    }

    public void setCalculatorLastState(boolean enable) {
        Global.putInt(this.mContext.getContentResolver(), "calculator_last_state", enable ? 1 : 0);
    }

    public boolean getCalculatorEnable() {
        return 1 == Global.getInt(this.mContext.getContentResolver(), "calculator_enable", 0);
    }

    public long queryTodayCount() {
        return StateSaverFactory.getSaver().query(this.mContext, "today_total_count", 0);
    }

    private void updateCount(long currentCount) {
        long lastSavedTime = queryLastSavedTime();
        long now = System.currentTimeMillis();
        int diff = countSpanTwoClockNum(lastSavedTime, now);
        Log.d("CalculatorModel", "updateCount(): diff = " + diff);
        long lastCount = StateSaverFactory.getSaver().query(this.mContext, "last_saved_count", 0);
        switch (diff) {
            case 0:
                if (this.mLastSavedCountInitialized) {
                    updateState("today_total_count", (queryTodayCount() + currentCount) - lastCount);
                    updateState("last_saved_time", now);
                    updateState("last_saved_count", currentCount);
                    return;
                }
                return;
            case 1:
                updateState("yesterday_count", queryTodayCount());
                updateState("today_total_count", 0);
                updateState("last_saved_time", now);
                updateState("last_saved_count", currentCount);
                showNotification();
                return;
            case 2:
                updateState("yesterday_count", 0);
                updateState("today_total_count", 0);
                updateState("last_saved_time", now);
                updateState("last_saved_count", currentCount);
                showNotification();
                return;
            default:
                Log.w("CalculatorModel", "nowTime < lastSavedTime");
                updateState("last_saved_time", now);
                updateState("last_saved_count", currentCount);
                return;
        }
    }

    public long queryYesterdayCount() {
        return StateSaverFactory.getSaver().query(this.mContext, "yesterday_count", 0);
    }

    private long queryLastSavedTime() {
        return StateSaverFactory.getSaver().query(this.mContext, "last_saved_time", 0);
    }

    private int countSpanTwoClockNum(long oldTime, long nowTime) {
        Calendar oldCalendar = Calendar.getInstance();
        oldCalendar.setTimeInMillis(oldTime);
        int lastYear = oldCalendar.get(1);
        int lastMonth = oldCalendar.get(2);
        int lastDay = oldCalendar.get(5);
        int lastHour = oldCalendar.get(11);
        Calendar.getInstance().setTimeInMillis(nowTime);
        Calendar day1 = Calendar.getInstance();
        day1.set(lastYear, lastMonth, lastDay, 2, 0, 0);
        long day1Time = day1.getTimeInMillis();
        day1Time -= day1Time % 1000;
        if (lastHour >= 2) {
            day1Time += 86400000;
        }
        if (nowTime >= day1Time + 86400000) {
            return 2;
        }
        if (nowTime >= day1Time) {
            return 1;
        }
        if (nowTime >= oldTime) {
            return 0;
        }
        return -1;
    }
}
