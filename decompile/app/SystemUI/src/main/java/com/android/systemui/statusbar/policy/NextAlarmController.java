package com.android.systemui.statusbar.policy;

import android.app.AlarmManager;
import android.app.AlarmManager.AlarmClockInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class NextAlarmController extends BroadcastReceiver {
    private AlarmManager mAlarmManager;
    private final ArrayList<NextAlarmChangeCallback> mChangeCallbacks = new ArrayList();
    private AlarmClockInfo mNextAlarm;

    public interface NextAlarmChangeCallback {
        void onNextAlarmChanged(AlarmClockInfo alarmClockInfo);
    }

    public NextAlarmController(Context context) {
        this.mAlarmManager = (AlarmManager) context.getSystemService("alarm");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.app.action.NEXT_ALARM_CLOCK_CHANGED");
        context.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        updateNextAlarm();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("NextAlarmController state:");
        pw.print("  mNextAlarm=");
        pw.println(this.mNextAlarm);
    }

    public void addStateChangedCallback(NextAlarmChangeCallback cb) {
        this.mChangeCallbacks.add(cb);
        cb.onNextAlarmChanged(this.mNextAlarm);
    }

    public void removeStateChangedCallback(NextAlarmChangeCallback cb) {
        this.mChangeCallbacks.remove(cb);
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.USER_SWITCHED") || action.equals("android.app.action.NEXT_ALARM_CLOCK_CHANGED")) {
            updateNextAlarm();
        }
    }

    private void updateNextAlarm() {
        this.mNextAlarm = this.mAlarmManager.getNextAlarmClock(-2);
        fireNextAlarmChanged();
    }

    private void fireNextAlarmChanged() {
        int n = this.mChangeCallbacks.size();
        for (int i = 0; i < n; i++) {
            ((NextAlarmChangeCallback) this.mChangeCallbacks.get(i)).onNextAlarmChanged(this.mNextAlarm);
        }
    }
}
