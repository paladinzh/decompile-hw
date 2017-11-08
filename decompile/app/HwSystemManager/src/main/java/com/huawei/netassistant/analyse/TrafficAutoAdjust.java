package com.huawei.netassistant.analyse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.NATAutoAdjustService;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.ShareCfg;
import com.huawei.systemmanager.util.HwLog;
import java.util.Calendar;
import java.util.Random;

public class TrafficAutoAdjust {
    public static final String ACTION_MAIN_AUTO_ADJUST = "huawei.intent.action.netassistant.autoadjust.maincard";
    public static final String ACTION_SECONDARY_AUTO_ADJUST = "huawei.intent.action.netassistant.autoadjust.secondarycard";
    private static final int ADJUST_BEGIN_HOUR = 8;
    private static final int ADJUST_END_HOUR = 22;
    public static final int QUERY_MAX_TIME_OUT = 300000;
    private static final String TAG = "TrafficAutoAdjust";
    private static TrafficAutoAdjust mSingleton;
    private static final Object sMutexTrafficAutoAdjust = new Object();
    private boolean isMainCardSameQuery = false;
    private boolean isSecondaryCardSameQuery = false;

    public static TrafficAutoAdjust getInstance() {
        TrafficAutoAdjust trafficAutoAdjust;
        synchronized (sMutexTrafficAutoAdjust) {
            if (mSingleton == null) {
                mSingleton = new TrafficAutoAdjust();
            }
            trafficAutoAdjust = mSingleton;
        }
        return trafficAutoAdjust;
    }

    public static void destroyInstance() {
        synchronized (sMutexTrafficAutoAdjust) {
            mSingleton = null;
        }
    }

    public boolean getIsMainSameQueryBooleanResult() {
        return this.isMainCardSameQuery;
    }

    public boolean getIsSecondarySameQueryBooleanResult() {
        return this.isSecondaryCardSameQuery;
    }

    public void setIsMainSameQueryBoolean(boolean result) {
        this.isMainCardSameQuery = result;
    }

    public void setIsSecondarySameQueryBoolean(boolean result) {
        this.isSecondaryCardSameQuery = result;
    }

    public boolean startAutoAdjust(String imsi) {
        HwLog.i(TAG, "start auto adjust");
        Context context = GlobalContext.getContext();
        Intent serviceIntent = new Intent(ShareCfg.SEND_ADJUST_SMS_ACTION);
        serviceIntent.putExtra(ShareCfg.EXTRA_SEND_SMS_IMSI, imsi);
        serviceIntent.setClass(context, NATAutoAdjustService.class);
        context.startService(serviceIntent);
        return true;
    }

    private long convertDBTimeToTriggerTime(long dbTime, long currentTime, int cycleDays) {
        long triggerTime;
        if (currentTime - dbTime <= 0) {
            triggerTime = currentTime;
        } else {
            triggerTime = dbTime + (((long) cycleDays) * 86400000);
        }
        triggerTime = getRightAdjustTime(triggerTime);
        long minDoTaskTime = currentTime + 1800000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(minDoTaskTime);
        if (calendar.get(11) >= 22) {
            minDoTaskTime = getRightAdjustTime(86400000 + minDoTaskTime);
        } else if (calendar.get(11) < 8) {
            minDoTaskTime = getRightAdjustTime(minDoTaskTime);
        }
        return Math.max(minDoTaskTime, triggerTime);
    }

    public void startAutoAdjustAlarm(Context context, int day, String imsi, long dbTime, long currTime, boolean isMainCard) {
        HwLog.v(TAG, "startAutoAdjustAlarm, ,Day: " + day + ",dbTime: " + dbTime + ",currTime: " + currTime);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        long autoAdjustTimeStamp = convertDBTimeToTriggerTime(dbTime, currTime, day);
        long intervalMillis = ((long) day) * 86400000;
        cancelAutoAdjustAlarm(context, imsi, isMainCard);
        alarmManager.setRepeating(1, autoAdjustTimeStamp, intervalMillis, createAutoAdjustIntent(context, imsi, isMainCard));
        HwLog.i(TAG, "set auto adjust alarm at " + DateUtil.millisec2String(autoAdjustTimeStamp));
    }

    private long getRightAdjustTime(long startAdjustStamp) {
        HwLog.i(TAG, "the db adjust time is " + DateUtil.millisec2String(startAdjustStamp));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startAdjustStamp);
        calendar.set(11, new Random().nextInt(14) + 8);
        HwLog.i(TAG, "the right adjust time is " + calendar.getTime().toString());
        return calendar.getTimeInMillis();
    }

    public PendingIntent createAutoAdjustIntent(Context context, String imsi, boolean isMainCard) {
        Intent intent = new Intent();
        if (isMainCard) {
            intent.setAction(ACTION_MAIN_AUTO_ADJUST);
            intent.setPackage(context.getPackageName());
            intent.putExtra(CommonConstantUtil.SIM_IMSI_FOR_TRANSPORT, imsi);
            return PendingIntent.getBroadcast(context, 104, intent, 134217728);
        }
        intent.setAction(ACTION_SECONDARY_AUTO_ADJUST);
        intent.setPackage(context.getPackageName());
        intent.putExtra(CommonConstantUtil.SIM_IMSI_FOR_TRANSPORT, imsi);
        return PendingIntent.getBroadcast(context, 105, intent, 134217728);
    }

    public void cancelAutoAdjustAlarm(Context context, String imsi, boolean isMainCard) {
        HwLog.v(TAG, "cancelAutoAdjustAlarm, is main card = " + isMainCard);
        ((AlarmManager) context.getSystemService("alarm")).cancel(createAutoAdjustIntent(context, imsi, isMainCard));
    }
}
