package com.android.deskclock.alarmclock;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.widget.Toast;
import com.android.deskclock.AlarmInitReceiver;
import com.android.deskclock.R;
import com.android.deskclock.RingCache;
import com.android.deskclock.alarmclock.Alarm.Columns;
import com.android.deskclock.alarmclock.Alarm.DaysOfWeek;
import com.android.deskclock.smartcover.HwCustCoverAdapter;
import com.android.util.ClockReporter;
import com.android.util.CompatUtils;
import com.android.util.Config;
import com.android.util.DayOfWeekRepeatUtil;
import com.android.util.FormatTime;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.ReflexUtil;
import com.android.util.Utils;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Alarms {
    static boolean mIsPowerOffAlarm = false;
    private static Alarm mPreAlarm = null;
    private static Set<Integer> mVRAlarmIds = new HashSet();
    private static long phoneBootTime = 0;
    private static boolean snoozeOffAlarm = false;

    public static void updateAlarmAlertTimeForOnlyType(android.content.ContentResolver r24, boolean r25) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00fe in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r4 = 6;
        r6 = new java.lang.String[r4];
        r4 = "_id";
        r5 = 0;
        r6[r5] = r4;
        r4 = "hour";
        r5 = 1;
        r6[r5] = r4;
        r4 = "minutes";
        r5 = 2;
        r6[r5] = r4;
        r4 = "daysofweek";
        r5 = 3;
        r6[r5] = r4;
        r4 = "alarmtime";
        r5 = 4;
        r6[r5] = r4;
        r4 = "daysofweektype";
        r5 = 5;
        r6[r5] = r4;
        r11 = 0;
        r5 = com.android.deskclock.alarmclock.Alarm.Columns.CONTENT_URI;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = "daysofweektype=? AND enabled";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = 1;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r8 = new java.lang.String[r4];	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = 0;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = java.lang.String.valueOf(r4);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r9 = 0;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r8[r9] = r4;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r9 = "hour, minutes ASC";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = r24;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r11 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        if (r11 == 0) goto L_0x00ff;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
    L_0x0043:
        r11.moveToFirst();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r10 = r11.getCount();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r15 = 0;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
    L_0x004b:
        if (r15 >= r10) goto L_0x00ff;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
    L_0x004d:
        r4 = 0;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r16 = r11.getInt(r4);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = 1;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r14 = r11.getInt(r4);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = 2;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r17 = r11.getInt(r4);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = 4;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r22 = r11.getLong(r4);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r12 = new com.android.deskclock.alarmclock.Alarm$DaysOfWeek;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = 3;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = r11.getInt(r4);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r12.<init>(r4);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r0 = r22;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r2 = r25;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r18 = isNeedUpdate(r12, r0, r2);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        if (r18 == 0) goto L_0x00d3;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
    L_0x0075:
        r0 = r17;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = calculateAlarm(r14, r0, r12);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r20 = r4.getTimeInMillis();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = "Alarms";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5.<init>();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = "updateAlarmAlertTimeForOnlyType : date = ";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = formatDate(r20);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = "  dayofweek = ";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = r12.queryDaysOfWeekCode();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        com.android.util.Log.iRelease(r4, r5);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r19 = new android.content.ContentValues;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r19.<init>();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = "alarmtime";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = java.lang.Long.valueOf(r20);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r0 = r19;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r0.put(r4, r5);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r4 = com.android.deskclock.alarmclock.Alarm.Columns.CONTENT_URI;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = "_id=?";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = 1;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = new java.lang.String[r7];	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r8 = java.lang.String.valueOf(r16);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r9 = 0;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7[r9] = r8;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r0 = r24;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r1 = r19;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r0.update(r4, r1, r5, r7);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
    L_0x00d3:
        r15 = r15 + 1;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r11.moveToNext();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        goto L_0x004b;
    L_0x00da:
        r13 = move-exception;
        r4 = "Alarms";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5.<init>();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = "updateAlarmAlertTimeForOnlyType use database exception : ";	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r7 = r13.getMessage();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.append(r7);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        com.android.util.Log.e(r4, r5);	 Catch:{ Exception -> 0x00da, all -> 0x0105 }
        if (r11 == 0) goto L_0x00fe;
    L_0x00fb:
        r11.close();
    L_0x00fe:
        return;
    L_0x00ff:
        if (r11 == 0) goto L_0x00fe;
    L_0x0101:
        r11.close();
        goto L_0x00fe;
    L_0x0105:
        r4 = move-exception;
        if (r11 == 0) goto L_0x010b;
    L_0x0108:
        r11.close();
    L_0x010b:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.deskclock.alarmclock.Alarms.updateAlarmAlertTimeForOnlyType(android.content.ContentResolver, boolean):void");
    }

    public static void addVRAlarm(int alarmId) {
        Log.iRelease("Alarms", "Alarms addVRAlarm " + alarmId);
        mVRAlarmIds.add(Integer.valueOf(alarmId));
    }

    public static void removeAllVRAlarm(Context context) {
        Iterator<Integer> iterator = mVRAlarmIds.iterator();
        Log.iRelease("Alarms", "removeAllVRAlarm size:" + mVRAlarmIds.size());
        if (iterator != null) {
            while (iterator.hasNext()) {
                cancleVRAlarm(context, ((Integer) iterator.next()).intValue());
                iterator.remove();
            }
            mVRAlarmIds.clear();
            SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
            Set<String> snoozedAlarms = new HashSet(prefs.getStringSet("snooze_ids", new HashSet()));
            for (String snoozedAlarm : snoozedAlarms) {
                Log.iRelease("Alarms", "snoozeAlarms  size:" + snoozedAlarms.size());
                int alarmId = Integer.parseInt(snoozedAlarm);
                long snoozeTime = prefs.getLong(getAlarmPrefSnoozeTimeKey(snoozedAlarm), 0);
                Alarm a = getAlarm(context.getContentResolver(), alarmId);
                if (a != null) {
                    a.showSnoozeNotification(context, snoozeTime, true);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("com.android.deskclock.updatealarmlist"));
                }
            }
        }
    }

    public static void notifyVRAlarm(Context context, Alarm alarm) {
        Log.iRelease("Alarms", "Alarms notifyVRAlarm");
        alarm.showNormalNotification(context);
        Intent playAlarm = new Intent("com.android.deskclock.ALARM_ALERT");
        playAlarm.putExtra("intent.extra.alarm", alarm);
        playAlarm.setClass(context, AlarmKlaxon.class);
        context.startService(playAlarm);
        LockAlarmFullActivity.setmIsServiceOn(true);
    }

    private static void cancleVRAlarm(Context context, int alarmId) {
        Log.iRelease("Alarms", "Alarms cancleVRAlarm");
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        if (nm != null) {
            nm.cancel(alarmId);
        }
    }

    public static boolean isSnoozeOffAlarm() {
        return snoozeOffAlarm;
    }

    public static void setSnoozeOffAlarm(boolean snoozeOffAlarm) {
        snoozeOffAlarm = snoozeOffAlarm;
    }

    public static Alarm getmPreAlarm() {
        return mPreAlarm;
    }

    public static void setmPreAlarm(Alarm a) {
        mPreAlarm = a;
    }

    public static long addAlarm(Context context, Alarm alarm) {
        snoozeOffAlarm = false;
        if (!alarm.insertDatabase(context)) {
            return 0;
        }
        long timeInMillis = alarm.calculateAlarm();
        if (alarm.queryAlarmEnable()) {
            clearSnoozeIfNeeded(context, timeInMillis);
            RingCache.getInstance().checkRingCache(context, false);
        }
        setNextAlert(context);
        reportOpenAlarmCount(context);
        return timeInMillis;
    }

    public static void deleteAlarm(Context context, int alarmId) {
        if (alarmId == -1) {
            Log.w("Alarms", "deleteAlarm : the alarm id is inval that will delete.");
            return;
        }
        ContentResolver contentResolver = context.getContentResolver();
        disableSnoozeAlert(context, alarmId);
        clearAutoSilent(context, alarmId);
        ((NotificationManager) context.getSystemService("notification")).cancel(alarmId);
        if (alarmId == Utils.getDefaultSharedPreferences(context).getInt("is_power_off_alarm_id", -1) && isAirplaneMode(context) == 0) {
            closeAirplaneMode(context);
        }
        Intent intent = new Intent();
        intent.setAction("com.android.deskclock.ALARM_DELETE");
        intent.putExtra("delete_alarm_id", alarmId);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
        Alarm alarm = getAlarm(contentResolver, alarmId);
        contentResolver.delete(ContentUris.withAppendedId(Columns.CONTENT_URI, (long) alarmId), "", null);
        HwLog.i("Alarms", "alarm is null  = " + (alarm == null));
        if (alarm != null) {
            RingCache.getInstance().deleteRingCache(context, alarm.alert, false);
        }
        reportOpenAlarmCount(context);
        setNextAlert(context);
    }

    public static int insertAlarm(Context context, Alarm alarm) {
        try {
            return (int) ContentUris.parseId(context.getContentResolver().insert(Columns.CONTENT_URI, createContentValues(alarm)));
        } catch (SQLiteFullException e) {
            Toast.makeText(context, context.getResources().getString(R.string.memory_full_Toast), 0).show();
            return -1;
        }
    }

    public static void reportOpenAlarmCount(Context context) {
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            try {
                ClockReporter.reportEventContainMessage(context, 65, "OPEN_ALARM_COUNT", cursor.getCount());
            } finally {
                cursor.close();
            }
        }
    }

    public static boolean isExistInDB(Context context, int alarmId) {
        Cursor cursor = context.getContentResolver().query(ContentUris.withAppendedId(Columns.CONTENT_URI, (long) alarmId), null, null, null, null);
        boolean isExist = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isExist = true;
            }
            cursor.close();
        }
        return isExist;
    }

    private static boolean isNeedUpdate(DaysOfWeek daysOfWeek, long time, boolean isPowerOffAlarm) {
        return daysOfWeek.isNeedUpdate(time, isPowerOffAlarm);
    }

    public static Cursor getAlarmsCursor(ContentResolver contentResolver) {
        return contentResolver.query(Columns.CONTENT_URI, new String[]{"_id", "hour", "minutes", "daysofweek", "alarmtime", "enabled", "vibrate", "volume", "message", "alert", "daysofweektype", "daysofweekshow"}, null, null, "hour, minutes ASC");
    }

    private static Cursor getFilteredAlarmsCursor(ContentResolver contentResolver) {
        return contentResolver.query(Columns.CONTENT_URI, new String[]{"_id", "hour", "minutes", "daysofweek", "alarmtime", "enabled", "vibrate", "volume", "message", "alert", "daysofweektype", "daysofweekshow"}, "enabled=1", null, null);
    }

    public static void closeAllAlarm(Context context) {
        ContentValues values = new ContentValues(1);
        values.put("enabled", Integer.valueOf(0));
        context.getContentResolver().update(Columns.CONTENT_URI, values, "enabled=?", new String[]{String.valueOf(1)});
        clearAllSnoozePreferences(context, Utils.getSharedPreferences(context, "AlarmClock", 0));
    }

    public static void closeAlarmById(Context context, int alarmId) {
        if (isExistInDB(context, alarmId)) {
            ContentValues values = new ContentValues(1);
            values.put("enabled", Integer.valueOf(0));
            disableSnoozeAlert(context, alarmId);
            context.getContentResolver().update(ContentUris.withAppendedId(Columns.CONTENT_URI, (long) alarmId), values, null, null);
            clearAutoSilent(context, alarmId);
        }
    }

    private static ContentValues createContentValues(Alarm alarm) {
        return alarm.createContentValues();
    }

    public static void clearSnoozeIfNeeded(Context context, long alarmTime) {
        SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
        List<String> snoozeAlarm = new ArrayList();
        for (String snoozedAlarm : prefs.getStringSet("snooze_ids", new HashSet())) {
            if (alarmTime < prefs.getLong(getAlarmPrefSnoozeTimeKey(snoozedAlarm), 0)) {
                snoozeAlarm.add(snoozedAlarm);
            }
        }
        int len = snoozeAlarm.size();
        for (int i = 0; i < len; i++) {
            clearSnoozePreference(context, prefs, Integer.parseInt((String) snoozeAlarm.get(i)));
        }
    }

    public static Alarm getAlarm(ContentResolver contentResolver, int alarmId) {
        String[] ALARM_QUERY_COLUMNS = new String[]{"_id", "hour", "minutes", "daysofweek", "alarmtime", "enabled", "vibrate", "volume", "message", "alert", "daysofweektype", "daysofweekshow"};
        Cursor cursor = contentResolver.query(ContentUris.withAppendedId(Columns.CONTENT_URI, (long) alarmId), ALARM_QUERY_COLUMNS, null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }

    public static void updateAlarmRingtone(Context context, Uri alarmAlert, int alarmId) {
        ContentValues values = new ContentValues();
        values.put("alert", alarmAlert == null ? "silent" : alarmAlert.toString());
        Log.d("Alarms", "updateAlarmRingtone : result = " + context.getContentResolver().update(ContentUris.withAppendedId(Columns.CONTENT_URI, (long) alarmId), values, null, null));
    }

    public static long setAlarm(Context context, Alarm alarm) {
        snoozeOffAlarm = false;
        alarm.clearAutoSilent(context);
        if (!alarm.updateAlarmDatabase(context, createContentValues(alarm))) {
            return 0;
        }
        long timeInMillis = alarm.calculateAlarm();
        alarm.disableSnoozeAlert(context, true);
        if (alarm.queryAlarmEnable()) {
            clearSnoozeIfNeeded(context, timeInMillis);
        }
        setNextAlert(context);
        return timeInMillis;
    }

    public static void enableAlarm(Context context, int id, boolean enabled) {
        enableAlarmInternal(context, id, enabled);
        setNextAlert(context);
    }

    public static void enableAlarmInternal(Context context, int id, boolean enabled) {
        enableAlarmInternal(context, getAlarm(context.getContentResolver(), id), enabled);
    }

    private static void enableAlarmInternal(Context context, Alarm alarm, boolean enabled) {
        if (alarm == null) {
            Log.w("Alarms", "enableAlarmInternal : the alarm is null, so we will return.");
            return;
        }
        int i;
        ContentValues values = new ContentValues(2);
        String str = "enabled";
        if (enabled) {
            i = 1;
        } else {
            i = 0;
        }
        values.put(str, Integer.valueOf(i));
        if (enabled) {
            long time = 0;
            if (!alarm.isRepeatSet()) {
                time = alarm.calculateAlarm();
            }
            values.put("alarmtime", Long.valueOf(time));
        } else {
            alarm.disableSnoozeAlert(context, false);
        }
        alarm.updateAlarmItem(context, values);
        RingCache.getInstance().updateRingCache(context, alarm.alert, enabled, true);
    }

    public static void clearAutoSilent(Context context, int id) {
        SharedPreferences mPreference = Utils.getDefaultSharedPreferences(context);
        if (mPreference.getInt(Integer.toString(id), 0) != 0) {
            Editor mEditor = mPreference.edit();
            mEditor.remove(Integer.toString(id));
            mEditor.commit();
        }
    }

    public static Alarm calculateNextAlert(Context context) {
        Alarm minTime = new Alarm(Long.MAX_VALUE);
        long now = System.currentTimeMillis();
        SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
        Set<Alarm> alarms = new HashSet();
        for (String snoozedAlarm : new HashSet(prefs.getStringSet("snooze_ids", new HashSet()))) {
            Alarm a = getAlarm(context.getContentResolver(), Integer.parseInt(snoozedAlarm));
            if (a != null) {
                alarms.add(a);
            } else {
                Log.w("Alarms", "calculateNextAlert : the snooze id can not find in provider.");
            }
        }
        Alarm alarm = null;
        for (Alarm a2 : newAlarms(context, alarms)) {
            if (a2 != null) {
                a2.calculateAlarmTime();
                updateAlarmTimeForSnooze(prefs, a2);
                if (a2.isBeforeNow(now)) {
                    Log.iRelease("Alarms", "calculateNextAlert : Disabling expired alarm set for " + Log.formatTime(a2.time));
                    if (a2.isRepeatSet()) {
                        Log.iRelease("Alarms", "calculateNextAlert : the alarm is dayOfWeek, so can not set its enable false.");
                        a2.disableSnoozeAlert(context, false);
                    } else {
                        a2.enableAlarmInternal(context, false);
                    }
                } else if (a2.isBeforeAlarm(minTime)) {
                    minTime = a2;
                    alarm = a2;
                }
            }
        }
        return alarm;
    }

    private static Set<Alarm> newAlarms(Context context, Set<Alarm> alarms) {
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        alarms.add(new Alarm(cursor));
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return alarms;
    }

    public static void disableExpiredAlarms(Context context) {
        if (context.getContentResolver() != null) {
            Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
            long now = System.currentTimeMillis();
            if (cur != null) {
                try {
                    if (cur.moveToFirst()) {
                        do {
                            Alarm alarm = new Alarm(cur);
                            if (!alarm.isTimeZero() && alarm.isBeforeNow(now)) {
                                Log.iRelease("Alarms", "disableExpiredAlarms : Disabling expired alarm set for " + formatDate(alarm.queryAlarmTime()));
                                if (alarm.isRepeatSet()) {
                                    Log.iRelease("Alarms", "disableExpiredAlarms : the alarm is dayOfWeek, so can not set its enable false.");
                                    alarm.disableSnoozeAlert(context, false);
                                } else {
                                    alarm.enableAlarmInternal(context, false);
                                }
                            }
                        } while (cur.moveToNext());
                    }
                } catch (Throwable th) {
                    if (cur != null) {
                        cur.close();
                    }
                }
            }
            if (cur != null) {
                cur.close();
            }
        }
    }

    public static void setNextAlert(Context context) {
        setNextAlert(context, false);
    }

    public static void setNextAlert(Context context, boolean switchChange) {
        Alarm alarm = calculateNextAlert(context);
        if (alarm != null) {
            if (!(switchChange ? alarm.isEqual(getNowAlarmTime(context)) : false)) {
                alarm.enableAlert(context);
                Log.iRelease("Alarms", "setNextAlert : old format alert time = " + formatDate(alarm.queryAlarmTime()) + "  new format alert time = " + alarm.getLogInfo(context));
                RingCache.getInstance().addRingCache(context, alarm.alert);
            }
        } else {
            Log.iRelease("Alarms", "setNextAlert : has not alarm in database, or not alarm enable.");
            disableAlert(context);
        }
        saveLastSetAlarmTime(context);
    }

    public static void enableAlert(Context context, Alarm alarm, long atTimeInMillis) {
        alarm.enableAlert(context, atTimeInMillis);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(atTimeInMillis);
        String timeString = formatDayAndTime(context, c);
        saveNextAlarm(context, timeString);
        saveNextHwAlarm(context, timeString, atTimeInMillis);
        setStatusBarIcon(context, true);
        alarm.setNowAlertAlarmId(context);
    }

    public static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService("alarm");
        Intent intent = new Intent("com.android.deskclock.ALARM_ALERT");
        if (UserHandle.getUserId(Binder.getCallingUid()) == 0) {
            intent.putExtra("remove_poweroff_alarm_anyway", true);
        }
        am.cancel(PendingIntent.getBroadcast(context, 0, intent, 134217728));
        saveNextAlarm(context, "");
        saveNextHwAlarm(context, "", 0);
        setStatusBarIcon(context, false);
    }

    public static void saveSnoozeAlert(Context context, int id, long time) {
        SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
        if (id == -1) {
            clearAllSnoozePreferences(context, prefs);
        } else {
            Set<String> snoozedIds = prefs.getStringSet("snooze_ids", new HashSet());
            snoozedIds.add(Integer.toString(id));
            Editor ed = prefs.edit();
            ed.putStringSet("snooze_ids", snoozedIds);
            ed.putLong(getAlarmPrefSnoozeTimeKey(id), time);
            ed.apply();
        }
        setNextAlert(context);
    }

    public static String getAlarmPrefSnoozeTimeKey(int id) {
        return getAlarmPrefSnoozeTimeKey(Integer.toString(id));
    }

    private static String getAlarmPrefSnoozeTimeKey(String id) {
        return "snooze_time" + id;
    }

    public static void disableSnoozeAlert(Context context, int id) {
        SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
        if (hasAlarmBeenSnoozed(prefs, id)) {
            clearSnoozePreference(context, prefs, id);
        }
    }

    private static void clearSnoozePreference(Context context, SharedPreferences prefs, int id) {
        String alarmStr = Integer.toString(id);
        Set<String> snoozedIds = prefs.getStringSet("snooze_ids", new HashSet());
        if (snoozedIds.contains(alarmStr)) {
            ((NotificationManager) context.getSystemService("notification")).cancel(id);
        }
        Editor ed = prefs.edit();
        snoozedIds.remove(alarmStr);
        ed.putStringSet("snooze_ids", snoozedIds);
        ed.remove(getAlarmPrefSnoozeTimeKey(alarmStr));
        ed.apply();
    }

    private static void clearAllSnoozePreferences(Context context, SharedPreferences prefs) {
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        Set<String> snoozedIds = prefs.getStringSet("snooze_ids", new HashSet());
        Editor ed = prefs.edit();
        for (String snoozeId : snoozedIds) {
            nm.cancel(Integer.parseInt(snoozeId));
            ed.remove(getAlarmPrefSnoozeTimeKey(snoozeId));
        }
        ed.remove("snooze_ids");
        ed.apply();
    }

    public static boolean hasAlarmBeenSnoozed(SharedPreferences prefs, int alarmId) {
        Set<String> snoozedIds = prefs.getStringSet("snooze_ids", null);
        return snoozedIds != null ? snoozedIds.contains(Integer.toString(alarmId)) : false;
    }

    private static boolean updateAlarmTimeForSnooze(SharedPreferences prefs, Alarm alarm) {
        return alarm.updateAlarmTimeForSnooze(prefs);
    }

    public static boolean enableSnoozeAlert(Context context) {
        SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
        int id = -1;
        List<String> ids = new ArrayList();
        for (String snoozedAlarm : prefs.getStringSet("snooze_ids", new HashSet())) {
            if (prefs.getLong(getAlarmPrefSnoozeTimeKey(snoozedAlarm), 0) > System.currentTimeMillis()) {
                ids.add(snoozedAlarm);
            }
        }
        if (ids.size() > 0) {
            Collections.sort(ids);
            try {
                id = Integer.valueOf(Integer.parseInt((String) ids.get(0))).intValue();
            } catch (NumberFormatException e) {
                Log.w("Alarms", "enableSnoozeAlert format id exception .");
            }
        }
        if (id == -1) {
            Log.iRelease("Alarms", "enableSnoozeAlert : can set a Alarm to system, because the id = -1");
            return false;
        }
        long time = prefs.getLong(getAlarmPrefSnoozeTimeKey(id), -1);
        Alarm alarm = getAlarm(context.getContentResolver(), id);
        Alarm nextAlarm = calculateNextAlert(context);
        if (alarm == null || nextAlarm == null) {
            Log.iRelease("Alarms", "enableSnoozeAlert : can not get alarm and can not calculate next Alert.");
            return false;
        }
        alarm.updateAlarmTime(time, nextAlarm);
        alarm.enableAlert(context);
        return true;
    }

    public static void setStatusBarIcon(Context context, boolean enabled) {
        if (UserHandle.getCallingUserId() == ActivityManager.getCurrentUser()) {
            Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
            alarmChanged.putExtra("alarmSet", enabled);
            if (AlarmInitReceiver.doGetBeKillAppEnd()) {
                context.sendStickyBroadcastAsUser(alarmChanged, Binder.getCallingUserHandle());
                AlarmInitReceiver.doSetBeKillAppEnd(false);
            } else {
                context.sendBroadcastAsUser(alarmChanged, Binder.getCallingUserHandle());
            }
        }
    }

    public static long calculateAlarm(Alarm alarm) {
        return alarm.calculateAlarm();
    }

    public static String formatDate(long millis) {
        return new SimpleDateFormat("yy/MM/dd HH:mm aaa EE", Locale.ENGLISH).format(new Date(millis));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Calendar calculateAlarmForWorkDay(int hour, int minute, DaysOfWeek daysOfWeek) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int nowHour = c.get(11);
        int nowMinute = c.get(12);
        int i = 0;
        if (hour < nowHour || (hour == nowHour && minute <= nowMinute)) {
            c.add(6, 1);
            Log.dRelease("Alarms", "calculateAlarmForWorkDay : over the next day.");
            i = 1;
        }
        while (true) {
            c.setTimeInMillis(System.currentTimeMillis() + (((long) i) * 86400000));
            c.set(11, hour);
            c.set(12, minute);
            c.set(13, 0);
            c.set(14, 0);
            int dayOfYear = c.get(6);
            int dayOfWeek = c.get(7);
            if (DayOfWeekRepeatUtil.judgetIsFreeOrWorkDay(dayOfYear - 1, c.getTimeInMillis()) == 1) {
                break;
            }
            if (DayOfWeekRepeatUtil.judgetIsFreeOrWorkDay(dayOfYear - 1, c.getTimeInMillis()) != 2) {
                if (dayOfWeek != 7 && dayOfWeek != 1) {
                    break;
                }
                Log.dRelease("Alarms", "calculateAlarmForWorkDay : it is sat or sun day.");
            } else {
                Log.dRelease("Alarms", "calculateAlarmForWorkDay : it is free day.");
            }
            i++;
        }
        return c;
    }

    public static Calendar calculateAlarm(int hour, int minute, DaysOfWeek daysOfWeek) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int nowHour = c.get(11);
        int nowMinute = c.get(12);
        if (hour < nowHour || (hour == nowHour && minute <= nowMinute)) {
            c.add(6, 1);
        }
        c.set(11, hour);
        c.set(12, minute);
        c.set(13, 0);
        c.set(14, 0);
        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0) {
            c.add(7, addDays);
        }
        return c;
    }

    static String formatTime(Context context, int hour, int minute, DaysOfWeek daysOfWeek, int dayOfWeekType) {
        Calendar c;
        if (dayOfWeekType == 4 && Utils.isChinaRegionalVersion() && DayOfWeekRepeatUtil.isHasWorkDayfn()) {
            c = calculateAlarmForWorkDay(hour, minute, daysOfWeek);
        } else {
            c = calculateAlarm(hour, minute, daysOfWeek);
        }
        return formatTime(context, c);
    }

    public static String formatTime(Context context, Calendar calendar) {
        if (calendar == null) {
            return "";
        }
        return new FormatTime(context, calendar).getTimeString(8);
    }

    private static String formatDayAndTime(Context context, Calendar c) {
        return c == null ? "" : (String) DateFormat.format(get24HourMode(context) ? "E kk:mm" : "E h:mm aa", c);
    }

    static void saveNextAlarm(Context context, String timeString) {
        if (CompatUtils.hasPermission(context, "android.permission.WRITE_SETTINGS")) {
            Log.iRelease("Alarms", "saveNextAlarm->has no WRITE_SETTINGS permission");
        }
        try {
            System.putString(context.getContentResolver(), "next_alarm_formatted", timeString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveNextHwAlarm(Context context, String timeString, long alarmTime) {
        if (CompatUtils.hasPermission(context, "android.permission.WRITE_SECURE_SETTINGS")) {
            Log.iRelease("Alarms", "saveNextHwAlarm->has no WRITE_SECURE_SETTINGS permission");
        }
        try {
            Secure.putString(context.getContentResolver(), "hw_next_alarm_formatted", timeString);
            Secure.putLong(context.getContentResolver(), "hw_next_alarm_time", alarmTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Alarm getNowAlertAlarmId(Context context) {
        int alarmId = Utils.getDefaultSharedPreferences(context).getInt("AlarmId", -1);
        if (alarmId == -1) {
            return null;
        }
        return getAlarm(context.getContentResolver(), alarmId);
    }

    public static String getNowAlarmFormatTime(Context context) {
        return Secure.getString(context.getContentResolver(), "hw_next_alarm_formatted");
    }

    public static long getNowAlarmTime(Context context) {
        long alarmTime = 0;
        try {
            alarmTime = Secure.getLong(context.getContentResolver(), "hw_next_alarm_time");
        } catch (SettingNotFoundException e) {
            Log.w("Alarms", "getNowAlarmTime : SettingNotFoundException = " + e.getMessage());
        }
        return alarmTime;
    }

    static boolean get24HourMode(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    private static final int queryMissedAlarmCount(Context context, long lastTime) {
        long nowTime = System.currentTimeMillis();
        if (0 != phoneBootTime) {
            nowTime = phoneBootTime;
        }
        if (nowTime < lastTime) {
            Log.iRelease("Alarms", "queryMissedAlarmCount : no missed alarm.");
            return 0;
        }
        Cursor cursorWork = context.getContentResolver().query(Columns.CONTENT_URI, null, "daysofweektype=? AND enabled", new String[]{String.valueOf(4)}, null);
        Cursor cursorNoWork = context.getContentResolver().query(Columns.CONTENT_URI, null, "daysofweektype!=? AND enabled", new String[]{String.valueOf(4)}, null);
        int countWork = getSmartCount(cursorWork, lastTime, nowTime);
        int countNoWork = getNormalCount(cursorNoWork, lastTime, nowTime);
        Log.iRelease("Alarms", "------>> countWork = " + countWork + " countNoWork = " + countNoWork + "total count = " + (countWork + countNoWork));
        return countNoWork + countWork;
    }

    private static int getSmartCount(Cursor cursor, long lastTime, long nowTime) {
        if (cursor != null) {
            return queryMissAlarmCount(cursor, lastTime, nowTime, true);
        }
        Log.dRelease("Alarms", "queryMissedAlarmCount->getSmartCount : can not query smart.");
        return 0;
    }

    private static int getNormalCount(Cursor cursor, long lastTime, long nowTime) {
        if (cursor == null) {
            Log.dRelease("Alarms", "queryMissedAlarmCount->getNormalCount : can not query normal.");
            return 0;
        } else if (cursor.getCount() != 0 && nowTime - lastTime <= 604800000) {
            return queryMissAlarmCount(cursor, lastTime, nowTime, false);
        } else {
            int count = cursor.getCount();
            cursor.close();
            Log.dRelease("Alarms", "queryMissedAlarmCount->getNormalCount : no enable alarm. or power off time much more than one weeks. 0, no missed alarm.");
            return count;
        }
    }

    private static int queryMissAlarmCount(Cursor cursor, long lastTime, long nowTime, boolean isSmart) {
        Calendar lastDate = Calendar.getInstance();
        lastDate.setTimeInMillis(lastTime);
        Calendar nowDate = Calendar.getInstance();
        nowDate.setTimeInMillis(nowTime);
        int offDay = lastDate.get(6);
        int bootDay = nowDate.get(6);
        int offDay_hour = lastDate.get(11);
        int bootDay_hour = nowDate.get(11);
        int offDay_minute = lastDate.get(12);
        int bootDay_minute = nowDate.get(12);
        int offDayOfWeekIndex = getDayOfWeekIndex(lastTime);
        int bootDayOfWeekIndex = getDayOfWeekIndex(nowTime);
        if (offDay == bootDay) {
            boolean isWorkDay = false;
            if (isSmart) {
                isWorkDay = isWorkDay(nowDate);
            }
            return queryMissedAlarmCount_AtSameDay(cursor, offDay_hour, bootDay_hour, offDay_minute, bootDay_minute, bootDayOfWeekIndex, isWorkDay);
        }
        boolean[] powerOffDaysOfWeek = new boolean[7];
        int[][] powerOffDaysOfYear = (int[][]) Array.newInstance(Integer.TYPE, new int[]{2, 1});
        if (!isSmart) {
            return queryMissedAlarmCount_InOneWeek(getPowerOffDaysOfWeek(lastDate, nowDate), cursor, offDay_hour, bootDay_hour, offDay_minute, bootDay_minute, offDayOfWeekIndex, bootDayOfWeekIndex, powerOffDaysOfYear);
        }
        powerOffDaysOfYear = getPowerOffDaysOfYears(lastDate, nowDate);
        if (powerOffDaysOfYear != null) {
            return queryMissedAlarmCount_InOneWeek(powerOffDaysOfWeek, cursor, offDay_hour, bootDay_hour, offDay_minute, bootDay_minute, offDayOfWeekIndex, bootDayOfWeekIndex, powerOffDaysOfYear);
        }
        cursor.close();
        return 0;
    }

    private static int[][] getPowerOffDaysOfYears(Calendar startDate, Calendar endDate) {
        int startDay = startDate.get(6);
        int endDay = endDate.get(6);
        if (startDay >= endDay) {
            return null;
        }
        int size = (endDay - startDay) + 1;
        long startTime = startDate.getTimeInMillis();
        int[][] alarmDays = (int[][]) Array.newInstance(Integer.TYPE, new int[]{2, size});
        Calendar c = startDate;
        int i = startDay;
        int j = 0;
        while (i <= endDay && j < size) {
            startDate.setTimeInMillis((((long) j) * 86400000) + startTime);
            alarmDays[1][j] = getDayOfWeekIndex(startDate.getTimeInMillis());
            if (isWorkDay(startDate)) {
                alarmDays[0][j] = 1;
            } else {
                alarmDays[0][j] = 0;
            }
            i++;
            j++;
        }
        return alarmDays;
    }

    private static boolean isWorkDay(Calendar c) {
        int dayOfYear = c.get(6);
        int dayOfWeek = c.get(7);
        if (Utils.isChinaRegionalVersion() && DayOfWeekRepeatUtil.judgetIsFreeOrWorkDay(dayOfYear - 1, c.getTimeInMillis()) == 1) {
            Log.dRelease("Alarms", "isWorkDay : it is work day.");
            return true;
        } else if (Utils.isChinaRegionalVersion() && DayOfWeekRepeatUtil.judgetIsFreeOrWorkDay(dayOfYear - 1, c.getTimeInMillis()) == 2) {
            Log.dRelease("Alarms", "isWorkDay : it is free day.");
            return false;
        } else if (dayOfWeek != 7 && dayOfWeek != 1) {
            return true;
        } else {
            Log.dRelease("Alarms", "isWorkDay : it is sat or sun day.");
            return false;
        }
    }

    private static final int getDayOfWeekIndex(long time) {
        int dayOfWeek = new Date(time).getDay();
        if (dayOfWeek == 0) {
            dayOfWeek = 7;
        }
        return dayOfWeek - 1;
    }

    private static final boolean[] getPowerOffDaysOfWeek(Calendar startDate, Calendar endDate) {
        int startDay = startDate.get(6);
        int endDay = endDate.get(6);
        if (endDay < startDay) {
            return new DaysOfWeek(0).getBooleanArray();
        }
        if (endDay - startDay >= 7) {
            return new DaysOfWeek(127).getBooleanArray();
        }
        DaysOfWeek daysOfWeek;
        int starDayOfWeek = getDayOfWeekIndex(startDate.getTimeInMillis());
        int endDayOfWeek = getDayOfWeekIndex(endDate.getTimeInMillis());
        int i;
        if (starDayOfWeek <= endDayOfWeek) {
            daysOfWeek = new DaysOfWeek(0);
            for (i = starDayOfWeek; i <= endDayOfWeek; i++) {
                daysOfWeek.set(i, true);
            }
        } else {
            daysOfWeek = new DaysOfWeek(127);
            for (i = endDayOfWeek + 1; i < starDayOfWeek; i++) {
                daysOfWeek.set(i, false);
            }
        }
        return daysOfWeek.getBooleanArray();
    }

    private static final void saveLastSetAlarmTime(Context context) {
        Editor editor = Utils.getSharedPreferences(context, "AlarmClock", 0).edit();
        editor.putLong("last_set_alarm_time", System.currentTimeMillis());
        editor.commit();
    }

    public static final void saveShutDownTime(Context context) {
        Editor editor = Utils.getSharedPreferences(context, "AlarmClock", 0).edit();
        editor.putLong("last_shut_down_time", System.currentTimeMillis());
        editor.commit();
    }

    public static final long getLastShutDownTime(Context context) {
        return Utils.getSharedPreferences(context, "AlarmClock", 0).getLong("last_shut_down_time", 0);
    }

    public static final void clearLastShutDownTimePref(Context context) {
        Editor ed = Utils.getSharedPreferences(context, "AlarmClock", 0).edit();
        ed.remove("last_shut_down_time");
        ed.apply();
    }

    public static final int notifyMissedAlarms(Context context, long lastTime) {
        int count = queryMissedAlarmCount(context, lastTime);
        showMissAlarmNotification(context, count);
        return count;
    }

    public static void showMissAlarmNotification(Context context, int count) {
        Log.iRelease("Alarms", "showMissAlarmNotification : count = " + count);
        if (count == 0) {
            NotificationManager notify = (NotificationManager) context.getSystemService("notification");
            if (notify != null) {
                notify.cancel(2184);
                return;
            }
            return;
        }
        NotificationManager notification = (NotificationManager) context.getSystemService("notification");
        if (notification != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, AlarmClock.class), 0);
            Notification status = new Notification(R.drawable.stat_notify_alarm, null, 0);
            status.flags |= 16;
            status.setLatestEventInfo(context, context.getResources().getQuantityString(R.plurals.missed_alarms_msg, count, new Object[]{Integer.valueOf(count)}), null, pendingIntent);
            notification.notify(2184, status);
        }
    }

    public static boolean needSetSnoozeAlert(Context context) {
        SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
        for (String snoozedAlarm : prefs.getStringSet("snooze_ids", new HashSet())) {
            if (prefs.getLong(getAlarmPrefSnoozeTimeKey(snoozedAlarm), 0) > System.currentTimeMillis()) {
                return true;
            }
        }
        return false;
    }

    private static int queryMissedAlarmCount_AtSameDay(Cursor cursor, int offDay_hour, int bootDay_hour, int offDay_minute, int bootDay_minute, int bootDayOfWeekIndex, boolean isWorkDay) {
        int count = 0;
        while (cursor.moveToNext()) {
            if (new Alarm(cursor).queryMissedAlarmCount_AtSameDay(offDay_hour, bootDay_hour, offDay_minute, bootDay_minute, bootDayOfWeekIndex, isWorkDay)) {
                count++;
            }
        }
        cursor.close();
        return count;
    }

    private static int queryMissedAlarmCount_InOneWeek(boolean[] powerOffDaysOfWeek, Cursor cursor, int offDay_hour, int bootDay_hour, int offDay_minute, int bootDay_minute, int offDayOfWeekIndex, int bootDayOfWeekIndex, int[][] powerOffDaysOfYear) {
        int count = 0;
        while (cursor.moveToNext()) {
            if (new Alarm(cursor).queryMissedAlarmCount_InOneWeek(powerOffDaysOfWeek, offDay_hour, bootDay_hour, offDay_minute, bootDay_minute, offDayOfWeekIndex, bootDayOfWeekIndex, powerOffDaysOfYear)) {
                count++;
            }
        }
        cursor.close();
        return count;
    }

    public static void setMIsPowerOffAlarm(boolean b) {
        mIsPowerOffAlarm = b;
    }

    public static boolean getMIsPowerOffAlarm() {
        return mIsPowerOffAlarm;
    }

    public static boolean alarmNowAlarm(Context context) {
        boolean result = false;
        if ("RTC".equals(SystemProperties.get("persist.sys.powerup_reason", "NORMAL")) || "1".equals(SystemProperties.get("sys.boot.reason", "0"))) {
            Config.doSetExit_count(1);
            if ("false".equals(SystemProperties.get("persist.sys.actualpoweron", "true"))) {
                Log.dRelease("Alarms", "alarmNowAlarm : quick power on");
                return false;
            }
            Alarm alarm = getPreAlarm(context);
            Log.w("Alarms", "alarmNowAlarm : getPreAlarm the alarm = " + alarm);
            mPreAlarm = alarm;
            Alarm nextAlarm = calculateNextAlert(context);
            if (mPreAlarm != null) {
                Log.w("Alarms", "alarmNowAlarm : getPreAlarm != null,  nextAlarm = " + nextAlarm);
                result = alarm.isPowerOn(nextAlarm);
            }
            return result;
        }
        Log.dRelease("Alarms", "alarmNowAlarm : RTC phone powerup not for alarm.");
        return false;
    }

    public static Alarm getPreAlarm(Context context) {
        Alarm alarm = null;
        Alarm maxTime = new Alarm(0);
        long now = System.currentTimeMillis();
        SharedPreferences prefs = Utils.getSharedPreferences(context, "AlarmClock", 0);
        Set<Alarm> alarms = new HashSet();
        for (String snoozedAlarm : prefs.getStringSet("snooze_ids", new HashSet())) {
            Alarm a = getAlarm(context.getContentResolver(), Integer.parseInt(snoozedAlarm));
            if (a != null) {
                alarms.add(a);
            } else {
                Log.w("Alarms", "getPreAlarm : the snooze id can not find in provider.");
            }
        }
        for (Alarm a2 : newAlarms(context, alarms)) {
            updateAlarmTimeForSnooze(prefs, a2);
            Log.w("Alarms", "getPreAlarm : a.time = " + Log.formatTime(a2.getTodayTimeMillon(true)) + " now = " + Log.formatTime(now));
            if (!a2.passOneSecond(now) && a2.betweenAlarms(maxTime, now)) {
                Log.w("Alarms", "getPreAlarm : in recently 5 minutes will ring.");
                maxTime = a2;
                alarm = a2;
                phoneBootTime = a2.beforeOneMinite();
            }
        }
        return alarm;
    }

    public static int isAirplaneMode(Context mContext) {
        int status = 0;
        Object executeMethod = ReflexUtil.executeStaticMethod("android.provider.Settings$Global", "getInt", new Class[]{ContentResolver.class, String.class, Integer.TYPE}, new Object[]{mContext.getContentResolver(), "user_set_airplane", Integer.valueOf(1)});
        if (executeMethod != null) {
            status = ((Integer) executeMethod).intValue();
        }
        Log.e("Alarms", "isAirplaneMode : user_set_airplane is " + status);
        return status;
    }

    public static void closeAirplaneMode(Context mContext) {
        Log.dRelease("Alarms", "closeAirplaneMode");
        Global.putInt(mContext.getContentResolver(), "airplane_mode_on", 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.putExtra("state", false);
        mContext.sendBroadcast(intent);
        Editor editor = Utils.getDefaultSharedPreferences(mContext).edit();
        editor.remove("is_power_off_alarm_id");
        editor.commit();
    }

    public static void saveAlertStatus(Context context, boolean isAlert) {
        Utils.getDefaultSharedPreferences(context).edit().putBoolean("isAlerting", isAlert).commit();
    }

    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        List<RunningServiceInfo> serviceList = ((ActivityManager) mContext.getSystemService("activity")).getRunningServices(300);
        if (serviceList == null || serviceList.size() <= 0) {
            return false;
        }
        for (RunningServiceInfo info : serviceList) {
            if (info.service != null && className.equals(info.service.getClassName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static boolean isPowerOnReasonForAlarm() {
        boolean isPowerOnForRTC = "RTC".equals(SystemProperties.get("persist.sys.powerup_reason", "NORMAL"));
        boolean isPowerOnForAlarmInMTK = "1".equals(SystemProperties.get("sys.boot.reason", "0"));
        Log.iRelease("Alarms", "isPowerOnReasonForAlarm : isPowerOnForRTC = " + isPowerOnForRTC + " | isPowerOnForAlarmInMTK = " + isPowerOnForAlarmInMTK);
        if (!isPowerOnForRTC && !isPowerOnForAlarmInMTK) {
            return false;
        }
        String packageName = SystemProperties.get("persist.sys.hwairplanestate", "error");
        if ("error".equals(packageName) || HwCustCoverAdapter.APP_PACKEGE.equals(packageName)) {
            return true;
        }
        return false;
    }

    public static boolean isPowerOffAlarm(Context context, int alarmId) {
        if (context == null || alarmId == -1) {
            return false;
        }
        boolean isPowerOffAlarm = !getMIsPowerOffAlarm() ? isSnoozeOffAlarm() : true;
        if (!isPowerOffAlarm) {
            isPowerOffAlarm = alarmId == Utils.getDefaultSharedPreferences(context).getInt("is_power_off_alarm_id", -1);
        }
        return isPowerOffAlarm;
    }

    public static boolean isContainWorkDayAlarm(ContentResolver contentResolver) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(Columns.CONTENT_URI, new String[]{"_id"}, "daysofweektype = 4", null, null);
            if (cursor != null) {
                boolean z = cursor.getCount() > 0;
                if (cursor != null) {
                    cursor.close();
                }
                return z;
            }
            if (cursor != null) {
                cursor.close();
            }
            return false;
        } catch (Exception e) {
            HwLog.e("Alarms", "query error " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
