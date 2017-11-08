package com.android.deskclock.alarmclock;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Process;
import android.provider.Settings.System;
import com.android.deskclock.AsyncHandler;
import com.android.deskclock.DeskClockApplication;
import com.android.util.HwLog;
import com.android.util.IMonitorWrapper;
import com.android.util.Log;
import com.android.util.Utils;

public class LogcatService {
    private static boolean isRun = false;

    public static final void judgeAlarms(final Context context) {
        AsyncHandler.post(new Runnable() {
            public void run() {
                Process.setThreadPriority(10);
                Log.iRelease("LogcatService", AsyncHandler.getState() + " onStartCommand : run thread_id = " + Thread.currentThread().getId());
                LogcatService.isRun = true;
                LogcatService.scanAlarmAlertException(context);
                Alarms.setNextAlert(DeskClockApplication.getDeskClockApplication());
                LogcatService.isRun = false;
            }
        });
    }

    private static final void scanAlarmAlertException(Context context) {
        long recentTime = Alarms.getNowAlarmTime(DeskClockApplication.getDeskClockApplication());
        boolean hasAlarm = Alarms.getNowAlarmFormatTime(DeskClockApplication.getDeskClockApplication()) == null;
        HwLog.i("LogcatService", " recentTime = " + recentTime + " format = " + Alarms.formatDate(recentTime));
        if (recentTime == 0 || hasAlarm) {
            Log.iRelease("LogcatService", "has not some alarm enable.");
            return;
        }
        long now = System.currentTimeMillis();
        HwLog.i("LogcatService", " now = " + now + " format = " + Alarms.formatDate(now));
        if (now < recentTime) {
            Log.iRelease("LogcatService", "has not some alarm to alarm.");
            return;
        }
        Alarm alarm = Alarms.getNowAlertAlarmId(DeskClockApplication.getDeskClockApplication());
        if (alarm == null || -1 == alarm.id) {
            HwLog.i("LogcatService", "Has no alarm can not alarm.");
            return;
        }
        String version = "5.0.0.1";
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            HwLog.w("LogcatService", "NameNotFoundException:" + e.getMessage());
        }
        boolean isAlertInSilent = System.getInt(context.getContentResolver(), "mode_ringer_streams_affected", 0) == 294;
        boolean isPowerOnReasonForAlarm = Alarms.isPowerOnReasonForAlarm();
        int powerOffId = Utils.getDefaultSharedPreferences(context).getInt("is_power_off_alarm_id", -1);
        if (isPowerOnReasonForAlarm && powerOffId == alarm.id) {
            HwLog.i("LogcatService", "power off alarm not alert.");
            IMonitorWrapper.reportPowerOffAlarmAlertEventFailed(version, 1, isAlertInSilent, recentTime, isPowerOnReasonForAlarm);
            return;
        }
        HwLog.i("LogcatService", "normal alarm not alert.");
        IMonitorWrapper.reportNormalAlarmAlertEventFailed(version, 1, isAlertInSilent, recentTime);
    }

    public static final boolean isRun() {
        return isRun;
    }
}
