package com.android.connection;

import android.content.Intent;
import com.android.deskclock.alarmclock.Alarm;
import com.android.deskclock.alarmclock.Alarms;
import com.android.util.ClockReporter;
import com.android.util.HwLog;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class AlarmWearableListenerService extends WearableListenerService {
    private int mAlarmID;
    private int mAlarmState;

    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent == null || messageEvent.getPath() == null) {
            HwLog.d("connection", "onMessageReceived() messageEvent is null!");
            return;
        }
        HwLog.d("connection", "onMessageReceived() messageEvent.getPath() = " + messageEvent.getPath());
        if ("/alarm_feedback".equals(messageEvent.getPath())) {
            DataMap dataMap = DataMap.fromByteArray(messageEvent.getData());
            this.mAlarmState = dataMap.getInt("alarm_state_feedback");
            this.mAlarmID = (int) dataMap.getLong("alarm_id_feedback");
            HwLog.d("connection", "mAlarmState = " + this.mAlarmState + ", mAlarmID = " + this.mAlarmID);
            if (this.mAlarmState == 0) {
                HwLog.d("connection", "handle alarm stop");
                handleAlarmStop();
            } else if (this.mAlarmState == 1) {
                HwLog.d("connection", "handle alarm snooze");
                handleAlarmSnooze();
            }
        }
    }

    public boolean needHandle() {
        AlarmState alarmState = AlarmState.getInstance();
        int state = alarmState.getState();
        if (state == 0) {
            return false;
        }
        if (alarmState.getAlramID() == this.mAlarmID) {
            return state == 1;
        } else {
            HwLog.d("connection", "not the same alarm, no handle");
            return false;
        }
    }

    public Alarm getAlarmByID() {
        Alarm alarm = Alarms.getAlarm(getContentResolver(), this.mAlarmID);
        if (alarm == null) {
            return null;
        }
        if (alarm.time == 0) {
            alarm.time = Alarms.calculateAlarm(alarm);
        }
        return alarm;
    }

    public void broadcastMsg(String action) {
        if (action != null) {
            if (action.equals("com.android.deskclock.watch_snooze_action") || action.equals("com.android.deskclock.watch_close_action")) {
                Intent intent = new Intent(action);
                Alarm alarm = getAlarmByID();
                if (alarm != null) {
                    ConnectionConstants.print(alarm);
                    intent.putExtra("intent.extra.alarm", alarm);
                    sendBroadcast(intent);
                }
            }
        }
    }

    public void handleAlarmStop() {
        if (needHandle()) {
            broadcastMsg("com.android.deskclock.watch_close_action");
            ClockReporter.reportEventMessage(this, 89, "");
            return;
        }
        HwLog.d("connection", "not need handle");
    }

    public void handleAlarmSnooze() {
        if (needHandle()) {
            broadcastMsg("com.android.deskclock.watch_snooze_action");
            ClockReporter.reportEventMessage(this, 88, "");
            return;
        }
        HwLog.d("connection", "not need handle");
    }
}
