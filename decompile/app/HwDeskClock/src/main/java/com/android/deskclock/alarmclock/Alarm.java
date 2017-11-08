package com.android.deskclock.alarmclock;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.Notification.DecoratedCustomViewStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.graphics.drawable.Icon;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.UserHandle;
import android.provider.BaseColumns;
import android.widget.Toast;
import com.android.deskclock.AlarmReceiver;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.util.DayOfWeekRepeatUtil;
import com.android.util.FormatTime;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.Calendar;
import java.util.Set;

public final class Alarm implements Parcelable {
    public static final Creator<Alarm> CREATOR = new Creator<Alarm>() {
        public Alarm createFromParcel(Parcel p) {
            return new Alarm(p);
        }

        public Alarm[] newArray(int size) {
            return new Alarm[size];
        }
    };
    public Uri alert;
    public DaysOfWeek daysOfWeek;
    public String daysOfWeekShow;
    public int daysOfWeekType;
    public boolean enabled;
    public int hour;
    public int id;
    public String label;
    public int minutes;
    public boolean silent;
    public long time;
    public boolean vibrate;
    public int volume;

    public static class Columns implements BaseColumns {
        private static final String[] ALARM_QUERY_COLUMNS = new String[]{"_id", "hour", "minutes", "daysofweek", "alarmtime", "enabled", "vibrate", "volume", "message", "alert", "daysofweektype", "daysofweekshow"};
        public static final Uri CONTENT_URI = Uri.parse("content://com.android.deskclock/alarm");
    }

    public static final class DaysOfWeek {
        private static int[] DAY_MAP = new int[]{2, 3, 4, 5, 6, 7, 1};
        private int mDays;

        public void set(int r1, boolean r2) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.deskclock.alarmclock.Alarm.DaysOfWeek.set(int, boolean):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 8 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.deskclock.alarmclock.Alarm.DaysOfWeek.set(int, boolean):void");
        }

        public DaysOfWeek(int days) {
            this.mDays = days;
        }

        public void setDaysOfWeek(boolean value, int... daysOfWeek) {
            for (int day : daysOfWeek) {
                set(day, value);
            }
        }

        private boolean isSet(int day) {
            return (this.mDays & (1 << day)) > 0;
        }

        public void set(DaysOfWeek dow) {
            this.mDays = dow.mDays;
        }

        public int getCoded() {
            return this.mDays;
        }

        public int queryDaysOfWeekCode() {
            return this.mDays;
        }

        public boolean[] getBooleanArray() {
            boolean[] ret = new boolean[7];
            for (int i = 0; i < 7; i++) {
                ret[i] = isSet(i);
            }
            return ret;
        }

        public boolean isRepeatSet() {
            return this.mDays != 0;
        }

        public int getNextAlarm(Calendar c) {
            if (this.mDays == 0) {
                return -1;
            }
            int today = (c.get(7) + 5) % 7;
            int dayCount = 0;
            while (dayCount < 7 && !isSet((today + dayCount) % 7)) {
                dayCount++;
            }
            return dayCount;
        }

        public boolean isNeedUpdate(long time, boolean isPowerOffAlarm) {
            if (this.mDays == 0) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(time);
                Calendar nowC = Calendar.getInstance();
                nowC.setTimeInMillis(System.currentTimeMillis());
                if (c.get(6) == nowC.get(6) && isPowerOffAlarm) {
                    return false;
                }
            }
            return true;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        int i;
        int i2 = 1;
        p.writeInt(this.id);
        if (this.enabled) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        p.writeInt(this.hour);
        p.writeInt(this.minutes);
        p.writeInt(this.daysOfWeek.getCoded());
        p.writeInt(this.daysOfWeekType);
        p.writeString(this.daysOfWeekShow);
        p.writeLong(this.time);
        if (this.vibrate) {
            i = 1;
        } else {
            i = 0;
        }
        p.writeInt(i);
        p.writeInt(this.volume);
        p.writeString(this.label);
        p.writeParcelable(this.alert, flags);
        if (!this.silent) {
            i2 = 0;
        }
        p.writeInt(i2);
    }

    public int queryAlarmId() {
        return this.id;
    }

    public void saveAlarmId(int id) {
        this.id = id;
    }

    public boolean queryAlarmEnable() {
        return this.enabled;
    }

    public void saveAlarmEnable(boolean isAble) {
        this.enabled = isAble;
    }

    public int queryAlarmHour() {
        return this.hour;
    }

    public void saveAlarmHour(int hourTime) {
        this.hour = hourTime;
    }

    public int qeuryAlarmMinites() {
        return this.minutes;
    }

    public void saveAlarmMinites(int minutesTime) {
        this.minutes = minutesTime;
    }

    public DaysOfWeek queryDaysOfWeek() {
        return this.daysOfWeek;
    }

    public void saveDaysOfWeek(DaysOfWeek days) {
        this.daysOfWeek = days;
    }

    public int queryDaysOfWeekType() {
        return this.daysOfWeekType;
    }

    public void saveDaysOfWeekType(int type) {
        this.daysOfWeekType = type;
    }

    public void savedaysOfWeekShow(String show) {
        this.daysOfWeekShow = show;
    }

    public long queryAlarmTime() {
        return this.time;
    }

    public boolean queryAlarmVibrate() {
        return this.vibrate;
    }

    public void saveAlarmVibrate(boolean bVibrate) {
        this.vibrate = bVibrate;
    }

    public void saveAlarmLabel(String lableString) {
        this.label = lableString;
    }

    public String queryAlarmlable() {
        return this.label;
    }

    public Uri queryAlarmAlert() {
        return this.alert;
    }

    public void saveAlarmAlert(Uri alarmAlert) {
        this.alert = alarmAlert;
    }

    public void saveVolume(int alarmVolume) {
        this.volume = alarmVolume;
    }

    public int queryVolume() {
        return this.volume;
    }

    public Alarm(Cursor c) {
        boolean z;
        boolean z2 = false;
        this.id = c.getInt(0);
        if (c.getInt(5) == 1) {
            z = true;
        } else {
            z = false;
        }
        this.enabled = z;
        this.hour = c.getInt(1);
        this.minutes = c.getInt(2);
        this.daysOfWeek = new DaysOfWeek(c.getInt(3));
        this.daysOfWeekType = c.getInt(10);
        this.daysOfWeekShow = c.getString(11);
        this.time = c.getLong(4);
        if (c.getInt(6) == 1) {
            z2 = true;
        }
        this.vibrate = z2;
        this.volume = c.getInt(7);
        this.label = c.getString(8);
        String alertString = c.getString(9);
        if ("silent".equals(alertString)) {
            this.silent = true;
            this.alert = Uri.parse(alertString);
            return;
        }
        if (!(alertString == null || alertString.length() == 0)) {
            this.alert = Uri.parse(alertString);
        }
        if (this.alert == null) {
            this.alert = RingtoneManager.getDefaultUri(4);
            Log.iRelease("Alarm", "If the database alert is null or it failed to parse. alertString = " + alertString);
        }
    }

    public Alarm(Parcel p) {
        boolean z;
        boolean z2 = true;
        this.id = p.readInt();
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.enabled = z;
        this.hour = p.readInt();
        this.minutes = p.readInt();
        this.daysOfWeek = new DaysOfWeek(p.readInt());
        this.daysOfWeekType = p.readInt();
        this.daysOfWeekShow = p.readString();
        this.time = p.readLong();
        if (p.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.vibrate = z;
        this.volume = p.readInt();
        this.label = p.readString();
        this.alert = (Uri) p.readParcelable(null);
        if (p.readInt() != 1) {
            z2 = false;
        }
        this.silent = z2;
    }

    public Alarm() {
        this.id = -1;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        this.hour = c.get(11);
        this.minutes = c.get(12);
        this.vibrate = true;
        this.daysOfWeek = new DaysOfWeek(0);
        this.daysOfWeekType = 0;
        this.daysOfWeekShow = AlarmSetDialogManager.getRepeatType(DeskClockApplication.getDeskClockApplication(), 0);
        this.alert = RingtoneManager.getDefaultUri(4);
    }

    public Alarm(long time) {
        this.time = time;
    }

    public String getLabelOrDefault(Context context) {
        if (this.label != null && this.label.length() != 0) {
            return this.label;
        }
        if (chekDefaultTag(context)) {
            return context.getString(R.string.default_alarm_label);
        }
        return context.getString(R.string.default_label);
    }

    private boolean chekDefaultTag(Context context) {
        if (3 != this.volume || this.hour < 7 || this.hour >= 12) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.id;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof Alarm)) {
            return false;
        }
        if (this.id == ((Alarm) o).id) {
            z = true;
        }
        return z;
    }

    public boolean insertDatabase(Context context) {
        try {
            Uri uri = context.getContentResolver().insert(Columns.CONTENT_URI, createContentValues());
            if (uri == null) {
                return false;
            }
            this.id = (int) ContentUris.parseId(uri);
            return true;
        } catch (SQLiteFullException e) {
            Toast.makeText(context, context.getResources().getString(R.string.memory_full_Toast), 0).show();
            return false;
        }
    }

    public boolean isDefaultAlarm() {
        return this.id == -1;
    }

    public boolean isUserDefineRing() {
        return this.daysOfWeekType == 3;
    }

    public ContentValues createContentValues() {
        ContentValues values = new ContentValues(8);
        if (!this.daysOfWeek.isRepeatSet()) {
            this.time = calculateAlarm();
        }
        values.put("enabled", Integer.valueOf(this.enabled ? 1 : 0));
        values.put("hour", Integer.valueOf(this.hour));
        values.put("minutes", Integer.valueOf(this.minutes));
        values.put("alarmtime", Long.valueOf(this.time));
        values.put("daysofweek", Integer.valueOf(this.daysOfWeek.getCoded()));
        values.put("daysofweektype", Integer.valueOf(this.daysOfWeekType));
        values.put("daysofweekshow", this.daysOfWeekShow);
        values.put("vibrate", Boolean.valueOf(this.vibrate));
        values.put("message", this.label);
        values.put("alert", this.alert == null ? "silent" : this.alert.toString());
        return values;
    }

    public long calculateAlarm() {
        if (this.daysOfWeekType == 4 && Utils.isChinaRegionalVersion() && DayOfWeekRepeatUtil.isHasWorkDayfn()) {
            return Alarms.calculateAlarmForWorkDay(this.hour, this.minutes, this.daysOfWeek).getTimeInMillis();
        }
        return Alarms.calculateAlarm(this.hour, this.minutes, this.daysOfWeek).getTimeInMillis();
    }

    public boolean queryMissedAlarmCount_AtSameDay(int offDay_hour, int bootDay_hour, int offDay_minute, int bootDay_minute, int bootDayOfWeekIndex, boolean isWorkDay) {
        boolean ret = false;
        if (this.hour < offDay_hour || (this.hour == offDay_hour && this.minutes <= offDay_minute)) {
            return false;
        }
        if (this.hour < bootDay_hour || (this.hour == bootDay_hour && this.minutes <= bootDay_minute)) {
            if (!this.daysOfWeek.isRepeatSet()) {
                ret = true;
            } else if (this.daysOfWeekType == 4) {
                if (!isWorkDay) {
                    return false;
                }
                ret = true;
            } else if (this.daysOfWeek.getBooleanArray()[bootDayOfWeekIndex]) {
                ret = true;
            }
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryMissedAlarmCount_InOneWeek(boolean[] powerOffDaysOfWeek, int offDay_hour, int bootDay_hour, int offDay_minute, int bootDay_minute, int offDayOfWeekIndex, int bootDayOfWeekIndex, int[][] powerOffDaysOfYear) {
        int i;
        if (this.daysOfWeekType == 4) {
            if (powerOffDaysOfYear != null) {
                for (i = 0; i < powerOffDaysOfYear[0].length; i++) {
                    if (powerOffDaysOfYear[0][i] == 1) {
                        if (powerOffDaysOfYear[1][i] == offDayOfWeekIndex) {
                            if (this.hour > offDay_hour || (this.hour == offDay_hour && this.minutes > offDay_minute)) {
                                Log.dRelease("Alarm", "off day after.....");
                                return true;
                            }
                        } else if (powerOffDaysOfYear[1][i] != bootDayOfWeekIndex) {
                            Log.dRelease("Alarm", "not day after.....");
                            return true;
                        } else if (this.hour < bootDay_hour || (this.hour == bootDay_hour && this.minutes <= bootDay_minute)) {
                            Log.dRelease("Alarm", "boot day after.....");
                            return true;
                        }
                    }
                }
            }
            return false;
        } else if (!this.daysOfWeek.isRepeatSet()) {
            return this.hour > offDay_hour || ((this.hour == offDay_hour && this.minutes > offDay_minute) || this.hour < bootDay_hour || (this.hour == bootDay_hour && this.minutes < bootDay_minute));
        } else {
            boolean[] daysOfWeekEnable = this.daysOfWeek.getBooleanArray();
            i = 0;
            while (i < 7) {
                if (powerOffDaysOfWeek[i] && daysOfWeekEnable[i]) {
                    if (i == offDayOfWeekIndex) {
                        if (this.hour > offDay_hour || (this.hour == offDay_hour && this.minutes > offDay_minute)) {
                            return true;
                        }
                    } else if (i != bootDayOfWeekIndex || this.hour < bootDay_hour || (this.hour == bootDay_hour && this.minutes <= bootDay_minute)) {
                        return true;
                    }
                }
                i++;
            }
            return false;
        }
    }

    public void calculateAlarmTime() {
        if (this.time == 0) {
            if (this.daysOfWeekType == 4 && Utils.isChinaRegionalVersion() && DayOfWeekRepeatUtil.isHasWorkDayfn()) {
                this.time = Alarms.calculateAlarmForWorkDay(this.hour, this.minutes, this.daysOfWeek).getTimeInMillis();
            } else {
                this.time = Alarms.calculateAlarm(this.hour, this.minutes, this.daysOfWeek).getTimeInMillis();
            }
        }
    }

    public boolean isBeforeNow(long now) {
        return this.time < now;
    }

    public boolean isEqual(long now) {
        return this.time == now;
    }

    public boolean isBeforeAlarm(Alarm now) {
        return this.time < now.time;
    }

    public boolean isTimeZero() {
        return this.time == 0;
    }

    public boolean isRepeatSet() {
        return this.daysOfWeek.isRepeatSet();
    }

    public void disableSnoozeAlert(Context context, boolean check) {
        if (!check) {
            Alarms.disableSnoozeAlert(context, this.id);
        } else if (this.enabled) {
            Alarms.disableSnoozeAlert(context, this.id);
        }
    }

    public void clearAutoSilent(Context context) {
        Alarms.clearAutoSilent(context, this.id);
    }

    public boolean updateAlarmDatabase(Context context, ContentValues values) {
        try {
            context.getContentResolver().update(ContentUris.withAppendedId(Columns.CONTENT_URI, (long) this.id), values, null, null);
            return true;
        } catch (SQLiteFullException e) {
            Toast.makeText(context, context.getResources().getString(R.string.memory_full_Toast), 0).show();
            return false;
        }
    }

    public void updateAlarmItem(Context context, ContentValues values) {
        try {
            context.getContentResolver().update(ContentUris.withAppendedId(Columns.CONTENT_URI, (long) this.id), values, null, null);
        } catch (SQLiteException e) {
            HwLog.w("Alarm", "updateAlarmItem error");
        }
    }

    public boolean isPowerOn(Alarm nextAlarm) {
        if (nextAlarm != null && this.id == nextAlarm.id && !this.daysOfWeek.isRepeatSet()) {
            return false;
        }
        Log.w("Alarm", "alarmNowAlarm : reset lastest alarm");
        return true;
    }

    public long getTodayTimeMillon(boolean setSelf) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(11, this.hour);
        c.set(12, this.minutes);
        c.set(13, 0);
        c.set(14, 0);
        int addDays = this.daysOfWeek.getNextAlarm(c);
        if (addDays > 0) {
            c.add(7, addDays);
        }
        long timeMillon = c.getTimeInMillis();
        if (setSelf) {
            this.time = timeMillon;
        }
        return timeMillon;
    }

    public boolean passOneSecond(long varTime) {
        return this.time - 1000 > varTime;
    }

    public void enableAlarmInternal(Context context, boolean enabled) {
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
            if (!this.daysOfWeek.isRepeatSet()) {
                time = calculateAlarm();
            }
            values.put("alarmtime", Long.valueOf(time));
        } else {
            disableSnoozeAlert(context, false);
        }
        updateAlarmItem(context, values);
    }

    public boolean betweenAlarms(Alarm alarm, long now) {
        return this.time > alarm.time && now - this.time < 300000;
    }

    public long beforeOneMinite() {
        return this.time - 60000;
    }

    public void showSnoozeNotification(Context context, long snoozeTime, boolean isLight) {
        Log.iRelease("Alarm", "show snooze notification");
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        nm.cancel(this.id);
        Icon icon = Utils.getBitampIcon(context, R.drawable.ic_notify_alarm);
        String label = context.getString(R.string.alarm_notify_snooze_label, new Object[]{getLabelOrDefault(context)});
        Calendar.getInstance().setTimeInMillis(snoozeTime);
        String message = FormatTime.checkFormatTimeString(context.getString(R.string.alarm_notify_snooze_context, new Object[]{Alarms.formatTime(context, c)}));
        Intent cancelSnooze = new Intent(context, AlarmReceiver.class);
        cancelSnooze.setAction("cancel_snooze");
        cancelSnooze.putExtra("intent.extra.alarm", this);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, this.id, cancelSnooze, 0);
        Builder builder = new Builder(context);
        builder.setSmallIcon(icon);
        builder.setTicker(label);
        builder.setContentTitle(label);
        builder.setVisibility(0);
        builder.setContentText(message);
        builder.setStyle(new DecoratedCustomViewStyle());
        builder.addAction(0, context.getString(R.string.alarm_notify_btn_cancel_snooze), broadcast);
        builder.setContentIntent(getToAlarmIntent(context));
        Notification n = builder.build();
        n.flags |= 2;
        n.defaults |= 2;
        if (isLight) {
            n.flags |= 1;
            n.defaults |= 4;
        }
        nm.notify(this.id, n);
    }

    public PendingIntent getToAlarmIntent(Context context) {
        Intent intent = new Intent(context, AlarmsMainActivity.class);
        intent.putExtra("deskclock.select.tab", 0);
        return PendingIntent.getActivity(context, hashCode(), intent, 134217728);
    }

    public void showNormalNotification(Context context) {
        String label = getLabelOrDefault(context);
        NotificationManager nm = (NotificationManager) context.getSystemService("notification");
        if (nm != null) {
            Notification n = new Notification(R.drawable.stat_notify_alarm, label, 0);
            n.setLatestEventInfo(context, label, "", null);
            n.flags |= 18;
            nm.notify(this.id, n);
        }
    }

    public void enableAlert(Context context) {
        Alarms.enableAlert(context, this, this.time);
    }

    public void enableAlert(Context context, long atTimeInMillis) {
        boolean z;
        AlarmManager am = (AlarmManager) context.getSystemService("alarm");
        Log.iRelease("Alarm", "enableAlert : setAlert id = " + this.id + " atTime = " + Alarms.formatDate(atTimeInMillis) + " alarmType = " + this.daysOfWeekType);
        Intent intent = new Intent("com.android.deskclock.ALARM_ALERT");
        Parcel out = Parcel.obtain();
        writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra("intent.extra.alarm_id", this.id);
        intent.putExtra("intent.extra.alarm_raw", out.marshall());
        String str = "is_owner_alarm";
        if (UserHandle.getUserId(Binder.getCallingUid()) == 0) {
            z = true;
        } else {
            z = false;
        }
        intent.putExtra(str, z);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 134217728);
        if (Utils.isMutiUser()) {
            am.setAlarmClock(new AlarmClockInfo(atTimeInMillis, null), sender);
        } else if (Utils.isKitKatOrLater()) {
            am.setExact(0, atTimeInMillis, sender);
        } else {
            am.set(0, atTimeInMillis, sender);
        }
        out.recycle();
    }

    public void setNowAlertAlarmId(Context context) {
        Editor editor = Utils.getDefaultSharedPreferences(context).edit();
        editor.putInt("AlarmId", this.id);
        editor.commit();
    }

    public String getLogInfo(Context context) {
        return Alarms.formatTime(context, this.hour, this.minutes, this.daysOfWeek, this.daysOfWeekType);
    }

    public boolean updateAlarmTimeForSnooze(SharedPreferences prefs) {
        boolean hasSnooze;
        Set<String> snoozedIds = prefs.getStringSet("snooze_ids", null);
        if (snoozedIds != null) {
            hasSnooze = snoozedIds.contains(Integer.toString(this.id));
        } else {
            hasSnooze = false;
        }
        if (!hasSnooze) {
            return false;
        }
        this.time = prefs.getLong(Alarms.getAlarmPrefSnoozeTimeKey(this.id), -1);
        return true;
    }

    public void updateAlarmTime(long theTime, Alarm alarm) {
        if (theTime > alarm.time) {
            theTime = alarm.time;
        }
        this.time = theTime;
    }
}
