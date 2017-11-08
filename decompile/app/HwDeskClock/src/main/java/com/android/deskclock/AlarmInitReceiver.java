package com.android.deskclock;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.SystemProperties;
import com.android.deskclock.alarmclock.Alarm;
import com.android.deskclock.alarmclock.Alarms;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.deskclock.stopwatch.StopWatchPage;
import com.android.util.Config;
import com.android.util.DayOfWeekRepeatUtil;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.Calendar;

public class AlarmInitReceiver extends BroadcastReceiver {
    @SuppressLint({"InlinedApi"})
    private static final String ACTION_BOOT_COMPLETED = (Utils.isNOrLater() ? "android.intent.action.LOCKED_BOOT_COMPLETED" : "android.intent.action.BOOT_COMPLETED");
    private static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (Config.getExit_count() == 0) {
                        Log.iRelease("AlarmInitReceiver", "handleMessage : AlarmInitReceiver, will exit app. Config.exit_count: " + Config.getExit_count());
                        AsyncHandler.quit();
                        Process.killProcess(Process.myPid());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private static boolean sBeKillAppEnd = false;
    private Context mContext;
    private Alarm mNextAlarm;
    private Alarm mPreAlarm;

    public static void doSetBeKillAppEnd(boolean beKillAppEnd) {
        sBeKillAppEnd = beKillAppEnd;
    }

    public static boolean doGetBeKillAppEnd() {
        return sBeKillAppEnd;
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.iRelease("AlarmInitReceiver", "AlarmInitReceiver->OnReceive : the intent is null or action is null.");
            return;
        }
        this.mContext = context;
        if ("hwbootcompleted".equals(intent.getType())) {
            Log.iRelease("AlarmInitReceiver", "AlarmInitReceiver->OnReceive : is power off Alarm, the type = hwbootcompleted");
            Config.doSetExit_count(1);
        }
        final String action = intent.getAction();
        Log.iRelease("AlarmInitReceiver", "OnReceive --> action:" + action);
        if ("com.huawei.systemmamanger.action.KILL_ROGAPP_END".equals(action)) {
            doSetBeKillAppEnd(true);
        }
        if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
            SharedPreferences prefs = Utils.getDefaultSharedPreferences(context);
            Log.d("AlarmInitReceiver", "AlarmInitReceiver->OnReceive : AlarmInitReceiver - Cleaning stopwatch data");
            Utils.clearSwSharedPref(prefs);
            Alarms.saveShutDownTime(context);
            Alarms.saveAlertStatus(context, false);
            StopWatchPage.setShutdown(true);
            RingCache.getInstance().clearOldCache(context);
            RingCache.getInstance().checkRingCache(context, false);
            return;
        }
        final PendingResult result = goAsync();
        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
        wl.acquire();
        final Context context2 = context;
        final Intent intent2 = intent;
        AsyncHandler.post(new Runnable() {
            public void run() {
                long lastTime = Alarms.getLastShutDownTime(context2);
                boolean isPowerOffAlarm = false;
                if (lastTime != 0) {
                    Alarms.clearLastShutDownTimePref(context2);
                    isPowerOffAlarm = true;
                    if (!Alarms.isPowerOnReasonForAlarm()) {
                        Alarms.notifyMissedAlarms(context2, lastTime);
                    } else if ("android.intent.action.TIMEZONE_CHANGED".equals(action) && "android.intent.action.TIME_SET".equals(action)) {
                        wl.release();
                        result.finish();
                        return;
                    }
                }
                AlarmInitReceiver.this.mPreAlarm = Alarms.getPreAlarm(context2);
                AlarmInitReceiver.this.mNextAlarm = Alarms.calculateNextAlert(context2);
                if ("android.intent.action.PRE_BOOT_COMPLETED".equals(action)) {
                    HwLog.i("AlarmInitReceiver", "boot pre completed , check cache");
                    RingCache.getInstance().clearOldCache(context2);
                    RingCache.getInstance().checkRingCache(context2, true);
                }
                int currentuser = ActivityManager.getCurrentUser();
                if (currentuser == 0 && AlarmInitReceiver.ACTION_BOOT_COMPLETED.equals(action)) {
                    if (Alarms.isPowerOnReasonForAlarm() || System.currentTimeMillis() < Alarms.getNowAlarmTime(context2)) {
                        Alarms.setStatusBarIcon(context2, true);
                    }
                    wl.release();
                    result.finish();
                    AlarmInitReceiver.this.checkKillSelf();
                    return;
                }
                boolean z = false;
                boolean z2 = false;
                if (currentuser != 0 && "android.intent.action.BOOT_COMPLETED".equals(action)) {
                    if (!DayOfWeekRepeatUtil.isLoadDate()) {
                        DayOfWeekRepeatUtil.initGetRestWork(context2);
                    }
                    z2 = Alarms.alarmNowAlarm(context2);
                    z = Alarms.needSetSnoozeAlert(context2);
                    if (!z) {
                        Alarms.saveSnoozeAlert(context2, -1, -1);
                        Log.dRelease("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : clear snooze alarm.");
                    }
                    Log.dRelease("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : alarmnow = " + z2 + " needSetSnoozeAlert = " + z);
                    Alarms.disableExpiredAlarms(context2);
                    Utils.clearSwSharedPref(Utils.getDefaultSharedPreferences(context2));
                }
                if ("com.android.calendar.downloaddatafinish".equals(action)) {
                    int status = intent2.getIntExtra("access_download_state", 0);
                    if (status == 1) {
                        DayOfWeekRepeatUtil.initGetRestWork(context2);
                    }
                    Log.dRelease("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : work data have finish. and result status = " + status);
                }
                if ("com.huawei.KoBackup.intent.action.RESTORE_COMPLETE".equals(action)) {
                    Alarms.updateAlarmAlertTimeForOnlyType(context2.getContentResolver(), isPowerOffAlarm);
                    Log.dRelease("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : the all backup has complete.");
                }
                AlarmInitReceiver.this.invalidateAlertData(context2, z2, z, action, 0, isPowerOffAlarm);
                wl.release();
                AlarmInitReceiver.this.checkKillSelf();
                result.finish();
            }
        });
    }

    private void checkKillSelf() {
        if (Config.getExit_count() == 0) {
            Calendar c;
            HwLog.i("AlarmInitReceiver", "check exit !!");
            boolean isNeedKill = true;
            if (mHandler.hasMessages(0)) {
                mHandler.removeMessages(0);
            }
            if (this.mNextAlarm != null) {
                c = Calendar.getInstance();
                c.setTimeInMillis(this.mNextAlarm.queryAlarmTime());
                Calendar oneMin = Calendar.getInstance();
                oneMin.add(12, 1);
                if (c.after(oneMin)) {
                    HwLog.i("AlarmInitReceiver", "next alarm is not in one minute");
                } else {
                    isNeedKill = false;
                    HwLog.i("AlarmInitReceiver", "next alarm in one minute will come");
                }
            }
            if (this.mPreAlarm != null) {
                Calendar preOneMin = Calendar.getInstance();
                preOneMin.add(12, -1);
                c = Calendar.getInstance();
                c.setTimeInMillis(this.mPreAlarm.queryAlarmTime());
                if (c.before(preOneMin)) {
                    HwLog.i("AlarmInitReceiver", "before alarm is not in one minute");
                } else {
                    isNeedKill = false;
                    HwLog.i("AlarmInitReceiver", "previous alarm in one minute will come");
                }
            }
            if (isNeedKill) {
                mHandler.sendEmptyMessageDelayed(0, 5000);
                return;
            }
            HwLog.i("AlarmInitReceiver", "start BootstrapService");
            BootstrapService.startBootstrapService(this.mContext);
        }
    }

    private void invalidateAlertData(Context context, boolean alarmnow, boolean needSetSnoozeAlert, String action, int count, boolean isPowerOffAlarm) {
        Log.iRelease("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : needSetSnoozeAlert = " + needSetSnoozeAlert + " alarmnow = " + alarmnow);
        if (needSetSnoozeAlert) {
            Alarms.enableSnoozeAlert(context);
        } else if (!alarmnow || Alarms.getmPreAlarm() == null) {
            if ("android.intent.action.TIMEZONE_CHANGED".equals(action) || "android.intent.action.TIME_SET".equals(action)) {
                Alarms.updateAlarmAlertTimeForOnlyType(context.getContentResolver(), isPowerOffAlarm);
            }
            Alarms.setNextAlert(context);
            Log.d("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : set next Alarm.");
        } else {
            String packageName = SystemProperties.get("persist.sys.hwairplanestate", "error");
            Log.w("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : enable alert 1s later. packageName = " + packageName);
            if ("error".equals(packageName) || HwCustCoverAdapter.APP_PACKEGE.equals(packageName)) {
                Alarms.enableAlert(context, Alarms.getmPreAlarm(), System.currentTimeMillis() + 1000);
            } else {
                Alarms.showMissAlarmNotification(context, count + 1);
                Alarms.setNextAlert(context);
            }
            Alarms.setmPreAlarm(null);
        }
        Log.d("AlarmInitReceiver", "AlarmInitReceiver->OnReceive->AsyncHandler : AlarmInitReceiver finished, Config.getExit_count() = " + Config.getExit_count());
    }
}
