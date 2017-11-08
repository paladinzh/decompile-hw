package com.android.deskclock;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Notification.DecoratedCustomViewStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.content.LocalBroadcastManager;
import android.vrsystem.IVRSystemServiceManager;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.android.connection.AlarmState;
import com.android.connection.ConnectionConstants;
import com.android.connection.WearUtils;
import com.android.deskclock.alarmclock.Alarm;
import com.android.deskclock.alarmclock.AlarmKlaxon;
import com.android.deskclock.alarmclock.Alarms;
import com.android.deskclock.alarmclock.CoverItemController;
import com.android.deskclock.alarmclock.LockAlarmFullActivity;
import com.android.deskclock.timer.TimerService;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.DayOfWeekRepeatUtil;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    public void onReceive(final Context context, final Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if ("com.android.deskclock.ALARM_ALERT".equals(intent.getAction())) {
                Log.iRelease("AlarmReceiver", "onReceive: action = ALARM_ALERT_ACTION");
                Config.doSetExit_count(1);
            }
            final PendingResult result = goAsync();
            AlarmAlertWakeLock.acquireCpuWakeLock(context);
            AsyncHandler.post(new Runnable() {
                public void run() {
                    AlarmReceiver.this.handleIntent(context, intent);
                    result.finish();
                }
            });
        }
    }

    public void sendLocalFinishBroadcast(Context context, Alarm alarm) {
        if (alarm != null && context != null) {
            Intent intent = new Intent("action_notify_finish_alert");
            intent.putExtra("intent.extra.alarm", alarm);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private void handleIntent(Context context, Intent intent) {
        AlarmState alarmState;
        Alarm intentAlarm;
        Alarm alarm;
        String action = intent.getAction();
        Log.iRelease("AlarmReceiver", "onReceive->handleIntent : action = " + action);
        if ("headup_snoose".equals(action) && intent.hasExtra("intent.extra.alarm")) {
            HwLog.d("connection", "AlarmReceiver receive headup message");
            alarmState = AlarmState.getInstance();
            int curState = alarmState.getState();
            intentAlarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
            if (intentAlarm != null) {
                ConnectionConstants.print(intentAlarm);
                if (alarmState.getAlramID() != intentAlarm.id) {
                    HwLog.i("connection", intent.getStringExtra("user") + "AlarmReceiver alarm is older.");
                } else if (curState != 1) {
                    HwLog.i("connection", "before this action, there is snooze or stop action");
                    return;
                } else if (intent.getStringExtra("user") != null) {
                    ClockReporter.reportEventMessage(context, 81, "");
                    HwLog.i("connection", "user click headup view to snooze alarm");
                    WearUtils.talkWithWatch(context, 3, intentAlarm);
                } else {
                    HwLog.i("connection", "AlarmKlaxon to snooze alarm");
                    WearUtils.talkWithWatch(context, 3, intentAlarm);
                }
                snooze(context, intentAlarm);
                sendLocalFinishBroadcast(context, intentAlarm);
            } else {
                return;
            }
        }
        if ("headup_close".equals(action) && intent.hasExtra("intent.extra.alarm")) {
            HwLog.d("connection", "AlarmReceiver receive headup view close messages");
            alarmState = AlarmState.getInstance();
            alarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
            if (alarm != null) {
                if (alarmState.getAlramID() != alarm.id) {
                    HwLog.i("connection", "the alarm is old");
                } else if (alarmState.getState() == 1) {
                    ClockReporter.reportEventMessage(context, 82, "");
                    HwLog.i("connection", "AlarmReceiver handle headup view close messages");
                    WearUtils.talkWithWatch(context, 2, alarm);
                }
                close(context, alarm);
                sendLocalFinishBroadcast(context, alarm);
            } else {
                return;
            }
        }
        if ("com.android.deskclock.ALARM_DELETE".equals(action)) {
            HwLog.d("connection", " AlarmReceiver receiver delete msg to stop alarm");
            alarmState = AlarmState.getInstance();
            int id = intent.getIntExtra("delete_alarm_id", -1);
            if (-1 == id) {
                HwLog.d("connection", "alarm id error");
                return;
            }
            alarm = new Alarm();
            alarm.id = id;
            if (alarmState.getAlramID() != alarm.id) {
                HwLog.d("connection", "LockAlarmFullActivity alarm is old, the newer come");
            } else if (alarmState.getState() == 1) {
                close(context, alarm);
                HwLog.d("connection", "AlarmReceiver handle  delete msg to stop alarm");
                WearUtils.talkWithWatch(context, 2, alarm);
            } else {
                HwLog.d("connection", "before this action, there is a snooze or close action");
            }
        }
        if ("com.android.deskclock.ALARM_CLOSE_NO_SNOOZE_ACTION".equals(action) && LockAlarmFullActivity.ismIsServiceOn()) {
            alarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
            if (alarm != null) {
                close(context, alarm);
            }
        }
        if ("alarm_killed".equals(action)) {
            killAlarm(context, intent);
        } else if ("cancel_snooze".equals(action)) {
            cancelSnooze(context, intent);
        } else if ("android.intent.action.timer_alert".equals(action)) {
            Intent serviceIntent = new Intent("android.intent.action.timer_alert");
            serviceIntent.setClass(context, TimerService.class);
            context.startService(serviceIntent);
        } else if ("com.android.deskclock.watch_snooze_action".equals(action)) {
            alarmState = AlarmState.getInstance();
            intentAlarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
            if (intentAlarm != null) {
                if (alarmState.getAlramID() != intentAlarm.id) {
                    HwLog.d("connection", "phone alarm id is different from the watch's");
                    return;
                }
                curAlarmState = alarmState.getState();
                if (curAlarmState > 2 || curAlarmState == 0 || curAlarmState == -1) {
                    HwLog.d("connection", "phone alarm want to do the same thing or state not arrive");
                } else {
                    snoozeFromWear(context, intentAlarm);
                }
            }
        } else if ("com.android.deskclock.watch_close_action".equals(action)) {
            alarmState = AlarmState.getInstance();
            intentAlarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
            if (intentAlarm != null && intentAlarm.id == alarmState.getAlramID()) {
                curAlarmState = alarmState.getState();
                if (curAlarmState != -1 && curAlarmState != 0 && curAlarmState != 3) {
                    closeFromWear(context, intentAlarm);
                }
            }
        } else if ("com.android.deskclock.ALARM_ALERT".equals(action)) {
            deskClockAlarmFire(context, intent);
        } else {
            Log.dRelease("AlarmReceiver", "handleIntent : Unknown intent, bail, action = " + action);
            AlarmAlertWakeLock.releaseCpuLock();
        }
    }

    public void deskClockAlarmFire(Context context, Intent intent) {
        if (context != null && intent != null) {
            Alarm alarm;
            Alarm tempAlarm = null;
            byte[] data = intent.getByteArrayExtra("intent.extra.alarm_raw");
            if (data != null) {
                alarm = getAlarm(data);
            } else {
                if (!DayOfWeekRepeatUtil.isLoadDate()) {
                    DayOfWeekRepeatUtil.initGetRestWork(context);
                }
                int powerAlarmId = intent.getIntExtra("intent.extra.alarm_id", -1);
                boolean isMissedAlarm = intent.getBooleanExtra("is_out_of_data_alarm", false);
                long powerAlarmTime = intent.getLongExtra("intent.extra.alarm_when", -1);
                Log.iRelease("AlarmReceiver", "powerAlarmId:" + powerAlarmId + "| isMissedAlarm:" + isMissedAlarm + "|time:" + powerAlarmTime);
                if (!handleMissAlarm(context, isMissedAlarm, Alarms.getLastShutDownTime(context))) {
                    if (-1 == powerAlarmId) {
                        Log.iRelease("AlarmReceiver", "Alarms.ALARM_ID never existed");
                        Alarms.setNextAlert(context);
                        return;
                    }
                    alarm = getPowerAlarm(context, powerAlarmId, powerAlarmTime);
                } else {
                    return;
                }
            }
            if (alarm == null) {
                Log.iRelease("AlarmReceiver", "handleIntent : Failed to parse the alarm from the intent");
                Alarms.setNextAlert(context);
                return;
            }
            Alarms.disableSnoozeAlert(context, alarm.id);
            long now = System.currentTimeMillis();
            if (now > alarm.time + 1800000) {
                tempAlarm = constructAlarm(context);
            }
            if (alarm.daysOfWeek.isRepeatSet()) {
                Alarms.setNextAlert(context);
            } else {
                Alarms.enableAlarm(context, alarm.id, false);
            }
            Log.iRelease("AlarmReceiver", "handleIntent : Recevied alarm set for " + Alarms.formatDate(alarm.time) + " now time = " + Alarms.formatDate(now));
            alarm = handleExceptionAlarm(context, alarm, tempAlarm, now);
            if (alarm != null) {
                AlarmAlertWakeLock.acquireCpuWakeLock(context);
                context.sendBroadcast(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
                showAlarmAlert(context, intent, alarm);
            }
        }
    }

    public void showAlarmAlert(Context context, Intent intent, Alarm alarm) {
        if (context != null && intent != null && alarm != null) {
            boolean isPowerOffAlarm = intent.getBooleanExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", false);
            HwLog.i("AlarmReceiver", "isPowerOffAlarm:" + isPowerOffAlarm);
            if (isPowerOffAlarm) {
                Editor editor = Utils.getDefaultSharedPreferences(context).edit();
                editor.putInt("is_power_off_alarm_id", alarm.id);
                editor.commit();
                Alarms.setMIsPowerOffAlarm(true);
            }
            boolean needShowContentView = needShowContentView(context);
            sendNotification(context, intent, alarm, needShowContentView);
            if (needShowContentView) {
                LockAlarmFullActivity.setmIsServiceOn(true);
                Intent playAlarm = new Intent("com.android.deskclock.ALARM_ALERT");
                playAlarm.putExtra("intent.extra.alarm", alarm);
                playAlarm.setClass(context, AlarmKlaxon.class);
                context.startService(playAlarm);
                AlarmState.getInstance().setState(1);
            }
            AlarmState alarmState = AlarmState.getInstance();
            alarmState.setAlramID(alarm.id);
            alarmState.setState(1);
            alarmState.setFireType(needShowContentView);
            WearUtils.talkWithWatch(context, 0, alarm);
            HwLog.d("connection", "AlarmReceiver a newer alram is coming!");
        }
    }

    public Alarm handleExceptionAlarm(Context context, Alarm alarm, Alarm tempAlarm, long now) {
        if (now > alarm.time + 1800000) {
            Log.iRelease("AlarmReceiver", "handleIntent : Ignoring stale alarm");
            if (tempAlarm == null) {
                Log.iRelease("AlarmReceiver", "handleIntent : Ignoring stale alarm");
                return null;
            }
            alarm = tempAlarm;
            if (!tempAlarm.daysOfWeek.isRepeatSet()) {
                Alarms.enableAlarm(context, tempAlarm.id, false);
            }
        }
        return alarm;
    }

    public Alarm getPowerAlarm(Context context, int powerAlarmId, long powerAlarmTime) {
        Alarm alarm = Alarms.getAlarm(context.getContentResolver(), powerAlarmId);
        if (powerAlarmTime != -1) {
            if (alarm != null) {
                alarm.time = powerAlarmTime;
            } else {
                Log.i("AlarmReceiver", "this alarm do not exit in database, alarm == null");
            }
        }
        return alarm;
    }

    public boolean handleMissAlarm(Context context, boolean isMissedAlarm, long lastTime) {
        if (!isMissedAlarm || Alarms.isPowerOnReasonForAlarm()) {
            return false;
        }
        if (lastTime != 0) {
            Log.iRelease("AlarmReceiver", "lastTime:" + Alarms.formatDate(lastTime));
            Alarms.notifyMissedAlarms(context, lastTime);
            Alarms.setNextAlert(context);
            Alarms.clearLastShutDownTimePref(context);
        }
        return true;
    }

    public Alarm getAlarm(byte[] data) {
        Parcel in = Parcel.obtain();
        in.unmarshall(data, 0, data.length);
        in.setDataPosition(0);
        Alarm alarm = (Alarm) Alarm.CREATOR.createFromParcel(in);
        in.recycle();
        return alarm;
    }

    public Alarm constructAlarm(Context context) {
        try {
            int id = Utils.getDefaultSharedPreferences(context).getInt("AlarmId", -1);
            if (id == -1) {
                return null;
            }
            HwLog.i("AlarmReceiver", "constructAlarm id = " + id);
            long alarmTime = Secure.getLong(context.getContentResolver(), "hw_next_alarm_time");
            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.setTimeInMillis(alarmTime);
            int alarmDay = alarmCalendar.get(6);
            int alarmHour = alarmCalendar.get(11);
            int alarmMinute = alarmCalendar.get(12);
            Calendar nowCalendar = Calendar.getInstance();
            int nowDay = nowCalendar.get(6);
            int nowHour = nowCalendar.get(11);
            int nowMinute = nowCalendar.get(12);
            if (alarmDay == nowDay && alarmHour == nowHour && alarmMinute == nowMinute) {
                Alarm alarm = Alarms.getAlarm(context.getContentResolver(), id);
                HwLog.i("AlarmReceiver", "construct Alarm sucessful");
                return alarm;
            }
            return null;
        } catch (SettingNotFoundException e) {
            HwLog.i("AlarmReceiver", "read fail from settings");
        } catch (Exception e2) {
            HwLog.i("AlarmReceiver", "construct Alarm fail");
        }
    }

    private boolean needShowContentView(Context context) {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService("keyguard");
        PowerManager pm = (PowerManager) context.getSystemService("power");
        boolean isKidMode = Global.getInt(context.getContentResolver(), "hwkidsmode_running", 0) == 1;
        boolean isVisibleCoverWin = Global.getInt(DeskClockApplication.getDeskClockApplication().getContentResolver(), LockAlarmFullActivity.SETTINGS_COVER_TYPE, 0) == 0;
        CoverItemController cController = CoverItemController.getInstance(context);
        int currentuserId = ActivityManager.getCurrentUser();
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        HwLog.i("AlarmReceiver", "keyguardManager.isKeyguardLocked() = " + keyguardManager.isKeyguardLocked() + " pm.isScreenOn() = " + pm.isScreenOn() + " isKidMode = " + isKidMode + " isVisibleCoverWin = " + isVisibleCoverWin + " cController.isCoverOpen() = " + cController.isCoverOpen());
        if (currentuserId != callingUserId || keyguardManager.isKeyguardLocked() || !pm.isScreenOn() || isKidMode || (isVisibleCoverWin && !cController.isCoverOpen())) {
            HwLog.i("AlarmReceiver", "will show full screen notification");
            return false;
        }
        HwLog.i("AlarmReceiver", "will show headsup notification");
        return true;
    }

    public static void sendNotification(Context context, Intent intent, Alarm alarm, boolean needHeadupView) {
        NotificationManager nm = getNotificationManager(context, alarm);
        if (nm != null) {
            String label = alarm.getLabelOrDefault(context);
            nm.notify(alarm.id, createNotification(context, label, createFullScreenIntent(context, intent, alarm), createAlertNotification(context, alarm, label), needHeadupView, alarm));
        }
    }

    public static void sendInnerNotification(Context context, Intent intent, Alarm alarm) {
        NotificationManager nm = getNotificationManager(context, alarm);
        if (nm != null) {
            nm.notify(alarm.id, constructNotification(context, alarm.getLabelOrDefault(context), createFullScreenIntent(context, intent, alarm), alarm));
        }
    }

    public static NotificationManager getNotificationManager(Context context, Alarm alarm) {
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        nm.cancel(alarm.id);
        HwLog.i("test", "sendNotification alarm.id =" + alarm.id);
        if (Utils.VR_SWITCH) {
            IVRSystemServiceManager vr = (IVRSystemServiceManager) context.getSystemService("vr_system");
            Log.iRelease("AlarmReceiver", "vr:" + vr + " alarmid:" + alarm.id);
            if (vr != null && vr.isVRMode()) {
                Alarms.notifyVRAlarm(context, alarm);
                Alarms.addVRAlarm(alarm.id);
                return null;
            }
        }
        return nm;
    }

    private static Notification createNotification(Context context, String label, PendingIntent fullScreenPendingIntent, RemoteViews contentView, boolean needHeadupView, Alarm alarm) {
        Notification n = constructNotification(context, label, fullScreenPendingIntent, alarm);
        if (needHeadupView) {
            n.priority = 1;
            n.defaults |= 2;
        } else {
            n.priority = 2;
            n.fullScreenIntent = fullScreenPendingIntent;
            if (n.fullScreenIntent == null) {
                Log.e("AlarmReceiver", "fullScreenIntent is null, may cause alarm has no alert UI");
            }
        }
        return n;
    }

    public static Notification constructNotification(Context context, String label, PendingIntent contentIntent, Alarm alarm) {
        Builder builder = new Builder(context);
        builder.setSmallIcon(Utils.getBitampIcon(context, R.drawable.ic_notify_alarm));
        builder.setTicker(label);
        builder.setContentTitle(label);
        builder.setVisibility(0);
        Calendar c = Calendar.getInstance();
        alarm.calculateAlarmTime();
        c.setTimeInMillis(alarm.time);
        builder.setContentText(Alarms.formatTime(context, c));
        builder.setStyle(new DecoratedCustomViewStyle());
        builder.addAction(0, context.getString(R.string.close), createCloseAlarmPendingIntent(context, alarm));
        builder.addAction(1, context.getString(R.string.sleep), createSleepAlarmPendingIntent(context, alarm));
        Notification n = builder.build();
        n.contentIntent = contentIntent;
        n.deleteIntent = createSleepAlarmPendingIntent(context, alarm);
        n.flags |= 3;
        n.defaults |= 4;
        return n;
    }

    private static Intent createIntent(Context context, Intent intent, Alarm alarm) {
        Intent fullScreenIntent = new Intent(context, LockAlarmFullActivity.class);
        fullScreenIntent.putExtra("intent.extra.alarm", alarm);
        fullScreenIntent.putExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", intent.getBooleanExtra("FLAG_IS_FIRST_POWER_OFF_ALARM", false));
        fullScreenIntent.setFlags(268697600);
        return fullScreenIntent;
    }

    private static PendingIntent createFullScreenIntent(Context context, Intent intent, Alarm alarm) {
        return PendingIntent.getActivity(context, alarm.id, createIntent(context, intent, alarm), 134217728);
    }

    private static RemoteViews createAlertNotification(Context context, Alarm alarm, String label) {
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.alarm_alerting_notification_big);
        contentView.setTextViewText(R.id.headup_label, label);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(alarm.time);
        contentView.setTextViewText(R.id.headup_alarmtime, Alarms.formatTime(context, c));
        contentView.setOnClickPendingIntent(R.id.icon_close, createCloseAlarmPendingIntent(context, alarm));
        contentView.setOnClickPendingIntent(R.id.icon_sleep, createSleepAlarmPendingIntent(context, alarm));
        return contentView;
    }

    public static PendingIntent createCloseAlarmPendingIntent(Context context, Alarm alarm) {
        Intent closeAlarm = new Intent(context, AlarmReceiver.class);
        closeAlarm.setAction("headup_close");
        closeAlarm.putExtra("intent.extra.alarm", alarm);
        return PendingIntent.getBroadcast(context, alarm.id, closeAlarm, 0);
    }

    public static PendingIntent createSleepAlarmPendingIntent(Context context, Alarm alarm) {
        Intent sleepAlarm = new Intent(context, AlarmReceiver.class);
        sleepAlarm.setAction("headup_snoose");
        sleepAlarm.putExtra("user", "user");
        sleepAlarm.putExtra("intent.extra.alarm", alarm);
        return PendingIntent.getBroadcast(context, alarm.id, sleepAlarm, 0);
    }

    public void snooze(Context context, Alarm alarm) {
        Alarms.setSnoozeOffAlarm(Alarms.getMIsPowerOffAlarm());
        int snoozeMinutes = Utils.getDefaultSharedPreferences(context).getInt("snooze_duration", 10);
        long snoozeTime = System.currentTimeMillis() + (((long) snoozeMinutes) * 60000);
        Log.iRelease("AlarmReceiver", "snooze : snoozeMinutes = " + snoozeMinutes + " snoozeTime = " + snoozeTime);
        Alarms.saveSnoozeAlert(context, alarm.id, snoozeTime);
        Alarms.clearAutoSilent(context, alarm.id);
        if (Alarms.getMIsPowerOffAlarm()) {
            Log.iRelease("AlarmReceiver", "snooze : is power off alarm, will shut down.");
        }
        alarm.showSnoozeNotification(context, snoozeTime, false);
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.android.deskclock.updatealarmlist"));
        if (LockAlarmFullActivity.ismIsServiceOn()) {
            Intent intent = new Intent("com.android.deskclock.ALARM_ALERT");
            intent.setClass(context, AlarmKlaxon.class);
            context.stopService(intent);
            LockAlarmFullActivity.setmIsServiceOn(false);
        }
    }

    public void close(Context context, Alarm alarm) {
        if (alarm.queryDaysOfWeekType() == 0) {
            RingCache.getInstance().deleteRingCache(context, alarm.alert, false);
        }
        Alarms.clearAutoSilent(context, alarm.id);
        ((NotificationManager) context.getSystemService("notification")).cancel(alarm.id);
        HwLog.d("connection", "AlarmReceiver need to close alarm");
        if (AlarmState.getInstance().getAlramID() == alarm.id) {
            HwLog.d("connection", "AlarmReceiver handle to close alarm");
            Intent intent = new Intent("com.android.deskclock.ALARM_ALERT");
            intent.setClass(context, AlarmKlaxon.class);
            context.stopService(intent);
            LockAlarmFullActivity.setmIsServiceOn(false);
        }
        if (Alarms.isPowerOffAlarm(context, alarm.id)) {
            Alarms.setSnoozeOffAlarm(false);
            if (Alarms.isAirplaneMode(context) == 0) {
                Alarms.closeAirplaneMode(context);
            }
            return;
        }
        if (alarm.label != null && "Start Alarm Test".equals(alarm.label) && alarm.vibrate && alarm.alert != null && "silent".equals(alarm.alert.toString())) {
            Log.iRelease("AlarmReceiver", "dismiss : it is cts test alarm, we will delete it after dismiss");
            Alarms.deleteAlarm(context, alarm.queryAlarmId());
        }
    }

    private void killAlarm(Context context, Intent intent) {
        Alarm mAlarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
        if (mAlarm == null) {
            Log.w("AlarmReceiver", "onReceive->handleIntent : the alarm that will kill is null, so we will return.");
            AlarmAlertWakeLock.releaseCpuLock();
            return;
        }
        SharedPreferences mPreference = Utils.getDefaultSharedPreferences(context);
        Editor mEditor = mPreference.edit();
        int mSnooze_num = mPreference.getInt("snooze_timers", 3);
        int mCount = mPreference.getInt(Integer.toString(mAlarm.id), 1);
        Log.dRelease("AlarmReceiver", "onReceive->handleIntent : mCount = " + mCount + " mSnooze_num = " + mSnooze_num);
        if (mCount < mSnooze_num) {
            mEditor.putInt(Integer.toString(mAlarm.id), mCount + 1);
            mEditor.commit();
            int snoozeMinutes = Utils.getDefaultSharedPreferences(context).getInt("snooze_duration", 10);
            long snoozeTime = System.currentTimeMillis() + (((long) snoozeMinutes) * 60000);
            Alarms.saveSnoozeAlert(context, mAlarm.id, snoozeTime);
            mAlarm.showSnoozeNotification(context, snoozeTime, true);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.android.deskclock.updatealarmlist"));
            String displayTime = context.getResources().getQuantityString(R.plurals.alarm_alert_snooze_set_Toast, Long.valueOf((long) snoozeMinutes).intValue(), new Object[]{Integer.valueOf(Long.valueOf((long) snoozeMinutes).intValue())});
            Log.dRelease("AlarmReceiver", "handleIntent : displayTime = " + displayTime);
            int themeID = context.getApplicationContext().getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
            if (themeID != 0) {
                context.getApplicationContext().setTheme(themeID);
            }
            Toast.makeText(context.getApplicationContext(), displayTime, 1).show();
        } else {
            Log.dRelease("AlarmReceiver", "onReceive->handleIntent : kill alarm by alarmcount over snoozenum.");
            Alarms.clearAutoSilent(context, mAlarm.id);
            getNotificationManager(context).cancel(mAlarm.id);
            if (Alarms.isPowerOffAlarm(context, mAlarm.id)) {
                Log.d("AlarmReceiver", "handleIntent->Alarms.ALARM_KILLED : is power off alarm, will closeAirp;aneMode.");
                if (Alarms.isAirplaneMode(context) == 0) {
                    Alarms.closeAirplaneMode(context);
                }
            }
            if (mAlarm.queryDaysOfWeekType() == 0) {
                RingCache.getInstance().deleteRingCache(context, mAlarm.alert, false);
            }
        }
        Log.dRelease("AlarmReceiver", "onReceive->handleIntent : judge if wil shut down phone = " + Alarms.getMIsPowerOffAlarm());
        if ((Alarms.getMIsPowerOffAlarm() || Alarms.isSnoozeOffAlarm()) && AlarmKlaxon.getCurrentAlarm() == null) {
            Log.w("AlarmReceiver", "onReceive->handleIntent : the alarm is null, for shut down phone alarm.");
        }
        AlarmAlertWakeLock.releaseCpuLock();
    }

    private void cancelSnooze(Context context, Intent intent) {
        Alarm alarm = null;
        Log.d("AlarmReceiver", "onReceive->handleIntent : cancel snooze.");
        if (intent.hasExtra("intent.extra.alarm")) {
            ClockReporter.reportEventMessage(context, 95, "");
            alarm = (Alarm) intent.getParcelableExtra("intent.extra.alarm");
        }
        if (alarm != null) {
            Alarms.clearAutoSilent(context, alarm.id);
            Alarms.disableSnoozeAlert(context, alarm.id);
            Alarms.setNextAlert(context);
        } else {
            Log.i("AlarmReceiver", "handleIntent : Unable to parse Alarm from intent.");
            Alarms.saveSnoozeAlert(context, -1, -1);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.android.deskclock.updatealarmlist"));
        int powerOffId = Utils.getDefaultSharedPreferences(context).getInt("is_power_off_alarm_id", -1);
        if (Alarms.getMIsPowerOffAlarm() || (alarm != null && powerOffId == alarm.id)) {
            Log.d("AlarmReceiver", "handleIntent->Alarms.CANCEL_SNOOZE : is power off alarm, will closeAirp;aneMode.");
            if (Alarms.isAirplaneMode(context) == 0) {
                Alarms.closeAirplaneMode(context);
            }
        }
        AlarmAlertWakeLock.releaseCpuLock();
    }

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService("notification");
    }

    private void snoozeFromWear(Context context, Alarm intentAlarm) {
        HwLog.d("connection", "snoozeFromWear");
        int curState = AlarmState.getInstance().getState();
        if (curState == 1) {
            AlarmState.getInstance().setState(2);
            snooze(context, intentAlarm);
            WearUtils.talkWithWatch(context, 3, intentAlarm);
        } else if (curState == 2) {
            HwLog.d("connection", "should not do anything, because phone is snoozing");
        }
    }

    private void closeFromWear(Context context, Alarm intentAlarm) {
        HwLog.d("connection", "closeFromWear");
        int curState = AlarmState.getInstance().getState();
        if (curState == 1) {
            AlarmState.getInstance().setState(3);
            close(context, intentAlarm);
            WearUtils.talkWithWatch(context, 2, intentAlarm);
        } else if (curState == 2) {
            HwLog.d("connection", "should not do anything, because phone is snoozing");
        }
    }
}
