package com.android.server.notification;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ScheduleInfo;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.server.notification.NotificationManagerService.DumpFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ScheduleConditionProvider extends SystemConditionProviderService {
    private static final String ACTION_EVALUATE = (SIMPLE_NAME + ".EVALUATE");
    public static final ComponentName COMPONENT = new ComponentName("android", ScheduleConditionProvider.class.getName());
    static final boolean DEBUG = true;
    private static final String EXTRA_TIME = "time";
    private static final String NOT_SHOWN = "...";
    private static final int REQUEST_CODE_EVALUATE = 1;
    private static final String SIMPLE_NAME = ScheduleConditionProvider.class.getSimpleName();
    static final String TAG = "ConditionProviders.SCP";
    private AlarmManager mAlarmManager;
    private boolean mConnected;
    private final Context mContext = this;
    private long mNextAlarmTime;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ScheduleConditionProvider.DEBUG) {
                Slog.d(ScheduleConditionProvider.TAG, "onReceive " + intent.getAction());
            }
            if ("android.intent.action.TIMEZONE_CHANGED".equals(intent.getAction())) {
                for (Uri conditionId : ScheduleConditionProvider.this.mSubscriptions.keySet()) {
                    ScheduleCalendar cal = (ScheduleCalendar) ScheduleConditionProvider.this.mSubscriptions.get(conditionId);
                    if (cal != null) {
                        cal.setTimeZone(Calendar.getInstance().getTimeZone());
                    }
                }
            }
            ScheduleConditionProvider.this.evaluateSubscriptions();
        }
    };
    private boolean mRegistered;
    private final ArrayMap<Uri, ScheduleCalendar> mSubscriptions = new ArrayMap();

    public ScheduleConditionProvider() {
        if (DEBUG) {
            Slog.d(TAG, "new " + SIMPLE_NAME + "()");
        }
    }

    public ComponentName getComponent() {
        return COMPONENT;
    }

    public boolean isValidConditionId(Uri id) {
        return ZenModeConfig.isValidScheduleConditionId(id);
    }

    public void dump(PrintWriter pw, DumpFilter filter) {
        pw.print("    ");
        pw.print(SIMPLE_NAME);
        pw.println(":");
        pw.print("      mConnected=");
        pw.println(this.mConnected);
        pw.print("      mRegistered=");
        pw.println(this.mRegistered);
        pw.println("      mSubscriptions=");
        long now = System.currentTimeMillis();
        for (Uri conditionId : this.mSubscriptions.keySet()) {
            pw.print("        ");
            pw.print(meetsSchedule((ScheduleCalendar) this.mSubscriptions.get(conditionId), now) ? "* " : "  ");
            pw.println(conditionId);
            pw.print("            ");
            pw.println(((ScheduleCalendar) this.mSubscriptions.get(conditionId)).toString());
        }
        SystemConditionProviderService.dumpUpcomingTime(pw, "mNextAlarmTime", this.mNextAlarmTime, now);
    }

    public void onConnected() {
        if (DEBUG) {
            Slog.d(TAG, "onConnected");
        }
        this.mConnected = true;
    }

    public void onBootComplete() {
    }

    public void onDestroy() {
        super.onDestroy();
        if (DEBUG) {
            Slog.d(TAG, "onDestroy");
        }
        this.mConnected = false;
    }

    public void onSubscribe(Uri conditionId) {
        if (DEBUG) {
            Slog.d(TAG, "onSubscribe " + conditionId);
        }
        if (ZenModeConfig.isValidScheduleConditionId(conditionId)) {
            this.mSubscriptions.put(conditionId, toScheduleCalendar(conditionId));
            evaluateSubscriptions();
            return;
        }
        notifyCondition(conditionId, 0, "badCondition");
    }

    public void onUnsubscribe(Uri conditionId) {
        if (DEBUG) {
            Slog.d(TAG, "onUnsubscribe " + conditionId);
        }
        this.mSubscriptions.remove(conditionId);
        evaluateSubscriptions();
    }

    public void attachBase(Context base) {
        attachBaseContext(base);
    }

    public IConditionProvider asInterface() {
        return (IConditionProvider) onBind(null);
    }

    private void evaluateSubscriptions() {
        if (this.mAlarmManager == null) {
            this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        }
        setRegistered(!this.mSubscriptions.isEmpty());
        long now = System.currentTimeMillis();
        this.mNextAlarmTime = 0;
        List<Condition> conditionsToNotify = new ArrayList();
        long nextUserAlarmTime = getNextAlarm();
        for (Uri conditionId : this.mSubscriptions.keySet()) {
            ScheduleCalendar cal = (ScheduleCalendar) this.mSubscriptions.get(conditionId);
            if (cal == null || !cal.isInSchedule(now)) {
                addToNotifyList(conditionId, 0, "!meetsSchedule", conditionsToNotify);
                if (nextUserAlarmTime == 0 && cal != null) {
                    cal.maybeSetNextAlarm(now, nextUserAlarmTime);
                }
            } else {
                addToNotifyList(conditionId, 1, "meetsSchedule", conditionsToNotify);
                cal.maybeSetNextAlarm(now, nextUserAlarmTime);
            }
            if (cal != null) {
                long nextChangeTime = cal.getNextChangeTime(now);
                if (nextChangeTime > 0 && nextChangeTime > now) {
                    if (this.mNextAlarmTime == 0 || nextChangeTime < this.mNextAlarmTime) {
                        this.mNextAlarmTime = nextChangeTime;
                    }
                }
            }
        }
        updateAlarm(now, this.mNextAlarmTime);
        if (conditionsToNotify.size() > 0) {
            notifyConditions((Condition[]) conditionsToNotify.toArray(new Condition[conditionsToNotify.size()]));
        }
    }

    private void updateAlarm(long now, long time) {
        AlarmManager alarms = (AlarmManager) this.mContext.getSystemService("alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, 1, new Intent(ACTION_EVALUATE).addFlags(268435456).putExtra(EXTRA_TIME, time), 134217728);
        alarms.cancel(pendingIntent);
        if (time > now) {
            if (DEBUG) {
                Slog.d(TAG, String.format("Scheduling evaluate for %s, in %s, now=%s", new Object[]{SystemConditionProviderService.ts(time), SystemConditionProviderService.formatDuration(time - now), SystemConditionProviderService.ts(now)}));
            }
            alarms.setExact(0, time, pendingIntent);
        } else if (DEBUG) {
            Slog.d(TAG, "Not scheduling evaluate");
        }
    }

    public long getNextAlarm() {
        AlarmClockInfo info = this.mAlarmManager.getNextAlarmClock(ActivityManager.getCurrentUser());
        return info != null ? info.getTriggerTime() : 0;
    }

    private static boolean meetsSchedule(ScheduleCalendar cal, long time) {
        return cal != null ? cal.isInSchedule(time) : false;
    }

    private static ScheduleCalendar toScheduleCalendar(Uri conditionId) {
        ScheduleInfo schedule = ZenModeConfig.tryParseScheduleConditionId(conditionId);
        if (schedule == null || schedule.days == null || schedule.days.length == 0) {
            return null;
        }
        ScheduleCalendar sc = new ScheduleCalendar();
        sc.setSchedule(schedule);
        sc.setTimeZone(TimeZone.getDefault());
        return sc;
    }

    private void setRegistered(boolean registered) {
        if (this.mRegistered != registered) {
            if (DEBUG) {
                Slog.d(TAG, "setRegistered " + registered);
            }
            this.mRegistered = registered;
            if (this.mRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.TIME_SET");
                filter.addAction("android.intent.action.TIMEZONE_CHANGED");
                filter.addAction(ACTION_EVALUATE);
                filter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
                filter.addAction("android.intent.action.USER_SWITCHED");
                registerReceiver(this.mReceiver, filter);
            } else {
                unregisterReceiver(this.mReceiver);
            }
        }
    }

    private void notifyCondition(Uri conditionId, int state, String reason) {
        if (DEBUG) {
            Slog.d(TAG, "notifyCondition " + conditionId + " " + Condition.stateToString(state) + " reason=" + reason);
        }
        notifyCondition(createCondition(conditionId, state));
    }

    private void addToNotifyList(Uri conditionId, int state, String reason, List<Condition> conditionsToNotify) {
        if (DEBUG) {
            Slog.d(TAG, "addToNotifyList " + conditionId + " " + Condition.stateToString(state) + " reason=" + reason);
        }
        if (conditionsToNotify != null) {
            conditionsToNotify.add(createCondition(conditionId, state));
        }
    }

    private Condition createCondition(Uri id, int state) {
        String summary = NOT_SHOWN;
        String line1 = NOT_SHOWN;
        String line2 = NOT_SHOWN;
        return new Condition(id, NOT_SHOWN, NOT_SHOWN, NOT_SHOWN, 0, state, 2);
    }
}
