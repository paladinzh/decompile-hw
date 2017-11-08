package com.android.deskclock;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import com.android.deskclock.alarmclock.Alarm;
import com.android.deskclock.alarmclock.Alarm.Columns;
import com.android.deskclock.alarmclock.Alarm.DaysOfWeek;
import com.android.deskclock.alarmclock.AlarmClock;
import com.android.deskclock.alarmclock.Alarms;
import com.android.deskclock.alarmclock.SetAlarm;
import com.android.util.Log;
import java.util.ArrayList;
import java.util.Calendar;

public class HandleSetAlarm extends Activity {
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String action = intent.getAction();
        if ("android.intent.action.SET_ALARM".equals(action)) {
            handleSetAlarm(intent);
            finish();
        } else if ("android.intent.action.SHOW_ALARMS".equals(action)) {
            handleShowAlarms();
            finish();
        } else if ("android.intent.action.SET_TIMER".equals(action)) {
            handleSetTimer(intent);
            finish();
        } else {
            if ("com.huawei.deskclock.android.intent.action.SET_ALARM".endsWith(action)) {
                handleSetAlarmFromOther(intent);
            } else if ("com.huawei.deskclock.android.intent.action.CLOSE_ALL_ALARM".equals(action)) {
                handleCloseAlarms(intent);
                finish();
            } else if (intent.hasExtra("android.intent.extra.alarm.HOUR")) {
                finish();
            } else {
                startActivity(new Intent(this, AlarmClock.class));
                finish();
            }
        }
    }

    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    private boolean handleCursorResult(Cursor c, long timeInMillis, boolean enable, boolean skipUi, DaysOfWeek daysOfWeek) {
        if (c == null || !c.moveToFirst()) {
            return false;
        }
        Alarm alarm = new Alarm(c);
        if (enable) {
            Alarms.enableAlarm(this, alarm.queryAlarmId(), true);
            alarm.saveAlarmEnable(true);
        }
        if (daysOfWeek.queryDaysOfWeekCode() > 0) {
            alarm.saveDaysOfWeekType(3);
            alarm.time = 0;
        } else {
            alarm.saveDaysOfWeekType(0);
        }
        alarm.saveDaysOfWeek(daysOfWeek);
        if (skipUi) {
            SetAlarm.popAlarmSetToast(DeskClockApplication.getDeskClockApplication(), timeInMillis);
            Alarms.setAlarm(this, alarm);
        } else {
            Intent i = new Intent(this, SetAlarm.class);
            i.putExtra("intent.extra.alarm", alarm);
            startActivity(i);
        }
        return true;
    }

    private String getMessageFromIntent(Intent intent) {
        String message = intent.getStringExtra("android.intent.extra.alarm.MESSAGE");
        return message == null ? "" : message;
    }

    private DaysOfWeek getDaysFromIntent(Intent intent) {
        DaysOfWeek daysOfWeek = new DaysOfWeek(0);
        ArrayList<Integer> days = intent.getIntegerArrayListExtra("android.intent.extra.alarm.DAYS");
        int[] daysArray;
        int i;
        if (days != null) {
            int size = days.size();
            daysArray = new int[size];
            for (i = 0; i < size; i++) {
                daysArray[i] = (((Integer) days.get(i)).intValue() + 5) % 7;
            }
            daysOfWeek.setDaysOfWeek(true, daysArray);
        } else {
            daysArray = intent.getIntArrayExtra("android.intent.extra.alarm.DAYS");
            if (daysArray != null) {
                int length = daysArray.length;
                int[] daysArrayNew = new int[length];
                for (i = 0; i < length; i++) {
                    daysArrayNew[i] = (daysArray[i] + 5) % 7;
                }
                daysOfWeek.setDaysOfWeek(true, daysArrayNew);
            }
        }
        return daysOfWeek;
    }

    private DaysOfWeek getDaysOfWeekFromIntent(Intent intent) {
        DaysOfWeek daysOfWeek = new DaysOfWeek(0);
        ArrayList<Integer> days = intent.getIntegerArrayListExtra("android.intent.extra.alarm.DAYS");
        if (days != null) {
            int size = days.size();
            int[] daysArray = new int[size];
            for (int i = 0; i < size; i++) {
                daysArray[i] = (((Integer) days.get(i)).intValue() + 7) % 7;
            }
            daysOfWeek.setDaysOfWeek(true, daysArray);
        }
        return daysOfWeek;
    }

    private void handleShowAlarms() {
        startActivity(new Intent(this, AlarmsMainActivity.class).putExtra("deskclock.select.tab", 0));
    }

    private void handleSetAlarm(Intent intent) {
        Throwable th;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Intent intent2 = intent;
        int hour = intent2.getIntExtra("android.intent.extra.alarm.HOUR", calendar.get(11));
        intent2 = intent;
        int minutes = intent2.getIntExtra("android.intent.extra.alarm.MINUTES", calendar.get(12));
        boolean skipUi = intent.getBooleanExtra("android.intent.extra.alarm.SKIP_UI", false);
        String message = getMessageFromIntent(intent);
        DaysOfWeek daysOfWeek = getDaysFromIntent(intent);
        boolean vibrate = intent.getBooleanExtra("android.intent.extra.alarm.VIBRATE", true);
        String alert = intent.getStringExtra("android.intent.extra.alarm.RINGTONE");
        long timeInMillis = Alarms.calculateAlarm(hour, minutes, daysOfWeek).getTimeInMillis();
        String[] ALARM_QUERY_COLUMNS = new String[]{"_id", "hour", "minutes", "daysofweek", "alarmtime", "enabled", "vibrate", "volume", "message", "alert", "daysofweektype", "daysofweekshow"};
        Cursor c;
        try {
            c = getContentResolver().query(Columns.CONTENT_URI, ALARM_QUERY_COLUMNS, "hour=" + hour + " AND " + "minutes" + "=" + minutes + " AND " + "daysofweek" + "=0 AND " + "message" + "=?", new String[]{message}, null);
            try {
                if (handleCursorResult(c, timeInMillis, true, skipUi, daysOfWeek)) {
                    finish();
                    Log.dRelease("HandleSetAlarm", "handleSetAlarm : have same alarm in provider for cts.");
                    if (c != null) {
                        c.close();
                    }
                    return;
                }
                if (c != null) {
                    c.close();
                }
                c = null;
                ContentValues values = new ContentValues();
                values.put("hour", Integer.valueOf(hour));
                values.put("minutes", Integer.valueOf(minutes));
                values.put("message", message);
                values.put("enabled", Integer.valueOf(1));
                values.put("vibrate", Boolean.valueOf(vibrate));
                int daysOfWeekCode = daysOfWeek.queryDaysOfWeekCode();
                if (daysOfWeekCode > 0) {
                    values.put("daysofweektype", Integer.valueOf(3));
                } else {
                    values.put("daysofweektype", Integer.valueOf(0));
                }
                values.put("daysofweek", Integer.valueOf(daysOfWeekCode));
                setTimeField(daysOfWeek, timeInMillis, values);
                if (alert == null) {
                    alert = "content://settings/system/alarm_alert";
                } else if (alert.isEmpty()) {
                    alert = "silent";
                }
                values.put("alert", alert);
                ContentResolver cr = getContentResolver();
                Uri result = cr.insert(Columns.CONTENT_URI, values);
                if (result != null) {
                    try {
                        c = cr.query(result, ALARM_QUERY_COLUMNS, null, null, null);
                        handleCursorResult(c, timeInMillis, false, skipUi, daysOfWeek);
                        Log.dRelease("HandleSetAlarm", "handleSetAlarm : create new alarm for cts.");
                    } finally {
                        if (c != null) {
                            c.close();
                        }
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                if (c != null) {
                    c.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            c = null;
            if (c != null) {
                c.close();
            }
            throw th;
        }
    }

    public void setTimeField(DaysOfWeek daysOfWeek, long timeInMillis, ContentValues values) {
        if (daysOfWeek.isRepeatSet()) {
            Log.i("HandleSetAlarm", "setTimeField set a repeate alarm");
            values.put("alarmtime", Integer.valueOf(0));
            return;
        }
        values.put("alarmtime", Long.valueOf(timeInMillis));
    }

    private void handleSetTimer(Intent intent) {
        if (intent.hasExtra("android.intent.extra.alarm.LENGTH")) {
            long length = 1000 * ((long) intent.getIntExtra("android.intent.extra.alarm.LENGTH", 0));
            if (length < 1000 || length > 86400000) {
                Log.iRelease("elephant", "Invalid timer length requested: " + length);
                return;
            }
            String label = getMessageFromIntent(intent);
            boolean skipUi = intent.getBooleanExtra("android.intent.extra.alarm.SKIP_UI", false);
            Log.dRelease("HandleSetAlarm", "handleSetTimer skipUi = " + skipUi + "  label = " + label + "  length = " + length);
            Intent timerIntent = new Intent(this, AlarmsMainActivity.class);
            timerIntent.setAction("com.android.deskclock.timer.otherstart");
            timerIntent.putExtra("deskclock.select.tab", 3);
            timerIntent.putExtra("timer.intent.extra", length);
            timerIntent.putExtra("timer_skip_ui", skipUi);
            timerIntent.putExtra("timer_skip_message", label);
            startActivity(timerIntent);
            return;
        }
        startActivity(new Intent(this, AlarmsMainActivity.class).putExtra("deskclock.select.tab", 3));
        Log.dRelease("HandleSetAlarm", "handleSetTimer 022");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.dRelease("HandleSetAlarm", "onActivityResult");
        if (100 == requestCode) {
            if (resultCode == -1) {
                Log.dRelease("HandleSetAlarm", "set alarm sucess and back ok.");
                Intent alarmIntent = new Intent();
                alarmIntent.putExtra("currentId", data != null ? data.getStringExtra("currentId") : Integer.valueOf(-1));
                setResult(-1, alarmIntent);
            } else if (resultCode == 0) {
                setResult(0);
            }
        }
        finish();
    }

    private Alarm buildAlarm(Intent intent) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = intent.getIntExtra("android.intent.extra.alarm.HOUR", calendar.get(11));
        int minutes = intent.getIntExtra("android.intent.extra.alarm.MINUTES", calendar.get(12));
        String message = getMessageFromIntent(intent);
        DaysOfWeek daysOfWeek = getDaysOfWeekFromIntent(intent);
        boolean vibrate = intent.getBooleanExtra("android.intent.extra.alarm.VIBRATE", true);
        String alert = intent.getStringExtra("android.intent.extra.alarm.RINGTONE");
        int alarmId = intent.getIntExtra("com.huawei.android.intent.extra.alarm.ALARMID", -1);
        boolean isSmartAlarm = intent.getBooleanExtra("com.huawei.android.intent.extra.alarm.IS_SMART", false);
        boolean enable = intent.getBooleanExtra("com.huawei.android.intent.extra.alarm.ENABLE", true);
        Alarm alarm = new Alarm();
        alarm.saveAlarmId(alarmId);
        alarm.saveAlarmHour(hour);
        alarm.saveAlarmMinites(minutes);
        alarm.saveAlarmEnable(enable);
        alarm.saveAlarmLabel(message);
        if (alert == null) {
            alarm.saveAlarmAlert(RingtoneHelper.getAvailableRingtone(this, Uri.parse("content://settings/system/alarm_alert")));
        } else if (alert.isEmpty()) {
            alarm.saveAlarmAlert(Uri.parse("silent"));
        } else {
            alarm.saveAlarmAlert(Uri.parse(alert));
        }
        alarm.saveAlarmVibrate(vibrate);
        if (isSmartAlarm) {
            alarm.saveDaysOfWeek(new DaysOfWeek(31));
            alarm.saveDaysOfWeekType(4);
        } else {
            alarm.saveDaysOfWeek(daysOfWeek);
            int daysOfWeekCode = daysOfWeek.queryDaysOfWeekCode();
            if (daysOfWeekCode == 127) {
                alarm.saveDaysOfWeekType(2);
            } else if (daysOfWeekCode == 31) {
                alarm.saveDaysOfWeekType(1);
            } else if (daysOfWeekCode > 0) {
                alarm.saveDaysOfWeekType(3);
            } else {
                alarm.saveDaysOfWeekType(0);
            }
        }
        return alarm;
    }

    private void handleSetAlarmFromOther(Intent intent) {
        Alarm alarm = buildAlarm(intent);
        if (intent.getBooleanExtra("android.intent.extra.alarm.SKIP_UI", false)) {
            if (alarm.isDefaultAlarm() || !Alarms.isExistInDB(this, alarm.queryAlarmId())) {
                alarm.saveAlarmId(Alarms.insertAlarm(this, alarm));
                Log.e("HandleSetAlarm", "handleSetAlarmFromOther : alarm.id = " + alarm.id);
            }
            if (alarm.queryAlarmEnable()) {
                SetAlarm.popAlarmSetToast(DeskClockApplication.getDeskClockApplication(), Alarms.calculateAlarm(alarm));
            }
            Alarms.setAlarm(this, alarm);
            Intent alarmIntent = new Intent();
            alarmIntent.putExtra("currentId", alarm.queryAlarmId());
            setResult(-1, alarmIntent);
            finish();
            return;
        }
        Intent i = new Intent(this, SetAlarm.class);
        i.putExtra("intent.extra.alarm", alarm);
        startActivityForResult(i, 100);
    }

    private void handleCloseAlarms(Intent intent) {
        ArrayList<Integer> ids = intent.getIntegerArrayListExtra("com.huawei.android.intent.extra.alarm.ALARMIDS");
        if (ids == null || ids.size() < 1) {
            Alarms.closeAllAlarm(this);
        } else {
            for (Integer id : ids) {
                if (id.intValue() == -1) {
                    Log.w("HandleSetAlarm", "The alarm id is Illegal.");
                } else {
                    Alarms.closeAlarmById(this, id.intValue());
                }
            }
        }
        Alarms.setNextAlert(this);
    }
}
