package com.android.connection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.provider.Settings.System;
import com.android.deskclock.alarmclock.Alarm;
import com.android.deskclock.alarmclock.AlarmKlaxon;
import com.android.util.HwLog;
import com.android.util.Utils;

public class WearUtils {
    public static void startActionAlarmFire(Context context, Long alarmTime, Long alarmId, Boolean bAlarmVibrate, Boolean bAlarmRing) {
        Intent intent = new Intent(context, AlarmWearableSendMsgService.class);
        intent.setAction("com.android.connection.action.alarm_fire");
        intent.putExtra("alarm_time", alarmTime);
        intent.putExtra("alarm_id", alarmId);
        if (2 == AlarmKlaxon.getCurrentZenMode(context)) {
            if (System.getInt(context.getContentResolver(), "mode_ringer_streams_affected", 0) != 294) {
                bAlarmRing = Boolean.valueOf(false);
            } else {
                HwLog.d("connection", "alarm will ring in slient, set in alarm setting");
            }
            bAlarmVibrate = Boolean.valueOf(false);
        }
        if (((AudioManager) context.getSystemService("audio")).getStreamVolume(4) == 0) {
            HwLog.d("connection", "the vloumn of alarm is 0!");
            bAlarmRing = Boolean.valueOf(false);
        }
        intent.putExtra("alarm_vibrate", bAlarmVibrate);
        intent.putExtra("alarm_ring", bAlarmRing);
        context.startService(intent);
    }

    public static void startActionHandleAlarm(Context context, Long alarmID, int alarmState) {
        Intent intent = new Intent(context, AlarmWearableSendMsgService.class);
        intent.setAction("com.android.connection.action.handle_alarm");
        intent.putExtra("alarm_id", alarmID);
        intent.putExtra("alarm_state", alarmState);
        context.startService(intent);
    }

    public static void startActionHandleMute(Context context, Long alarmID, boolean bMute) {
        Intent intent = new Intent(context, AlarmWearableSendMsgService.class);
        intent.setAction("com.android.connection.action.mute_alarm");
        intent.putExtra("alarm_id", alarmID);
        intent.putExtra("alarm_mute", bMute);
        context.startService(intent);
    }

    public static void talkWithWatch(Context ctx, int type, Alarm alarm) {
        if (alarm != null) {
            switch (type) {
                case 0:
                    sendStartMessage(ctx, alarm);
                    break;
                case 1:
                    if (!isNeedSnooze(ctx, alarm)) {
                        changAlarmState(alarm, 3);
                        sendStopMessage(ctx, alarm);
                        break;
                    }
                    changAlarmState(alarm, 2);
                    sendSnoozeMessage(ctx, alarm);
                    break;
                case 2:
                    changAlarmState(alarm, 3);
                    sendStopMessage(ctx, alarm);
                    break;
                case 3:
                    changAlarmState(alarm, 2);
                    sendSnoozeMessage(ctx, alarm);
                    break;
            }
        }
    }

    private static void changAlarmState(Alarm alarm, int state) {
        AlarmState alarmState = AlarmState.getInstance();
        if (alarmState.getAlramID() == alarm.id) {
            alarmState.setState(state);
        }
    }

    private static void sendStartMessage(Context ctx, Alarm alarm) {
        ConnectionConstants.print(alarm);
        startActionAlarmFire(ctx, Long.valueOf(alarm.time), Long.valueOf((long) alarm.id), Boolean.valueOf(alarm.vibrate), Boolean.valueOf(!alarm.silent));
    }

    private static void sendSnoozeMessage(Context ctx, Alarm alarm) {
        ConnectionConstants.print(alarm);
        startActionHandleAlarm(ctx, Long.valueOf((long) alarm.id), 1);
    }

    private static void sendStopMessage(Context ctx, Alarm alarm) {
        ConnectionConstants.print(alarm);
        startActionHandleAlarm(ctx, Long.valueOf((long) alarm.id), 0);
    }

    public static boolean isNeedSnooze(Context ctx, Alarm alarm) {
        SharedPreferences pref = Utils.getDefaultSharedPreferences(ctx);
        int snoozeNum = pref.getInt("snooze_timers", 3);
        int hadSnooze = pref.getInt(Integer.toString(alarm.id), 1);
        HwLog.d("connection", "snoozeNum =" + snoozeNum + ", hadSnooze =" + hadSnooze);
        if (hadSnooze < snoozeNum) {
            return true;
        }
        return false;
    }
}
