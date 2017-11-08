package com.android.connection;

import com.android.deskclock.alarmclock.Alarm;
import com.android.util.HwLog;

public class ConnectionConstants {
    public static void print(Alarm alarm) {
        long id = (long) alarm.id;
        boolean silent = alarm.silent;
        boolean vibrate = alarm.vibrate;
        HwLog.d("connection", "id=" + id + ", slient=" + silent + ", vibrate=" + vibrate + ", time=" + alarm.time);
    }
}
