package com.huawei.thermal.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

public final class BroadcastAdapter {
    private BroadcastAdapter() {
    }

    public static void sendThermalUIEvent(Context context, String event) {
        if (context != null && event != null) {
            Intent intent = new Intent("huawei.intent.action.THERMAL_UI_EVENT");
            intent.putExtra("Event", event);
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
            Log.i("BroadcastAdapter", "send Thermal event = " + event);
        }
    }

    public static void sendThermalComUIEvent(Context context, String event, String flag) {
        if (context != null && event != null) {
            Intent intent = new Intent("huawei.intent.action.COM_THERMAL_UI_EVENT");
            intent.putExtra("Event", event);
            if (flag != null) {
                intent.putExtra("Flag", flag);
            }
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
            Log.i("BroadcastAdapter", "send Com Thermal event = " + event + " flag =" + flag);
        }
    }

    public static void sendVRWarningLevel(Context context, String key, String value) {
        if (context != null && key != null) {
            Intent intent = new Intent("huawei.intent.action.VR_THERMAL_WARNING");
            intent.putExtra(key, value);
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
            Log.i("BroadcastAdapter", "send VR warning level (" + key + ")(" + value + ")");
        }
    }

    public static void sendLowTempWarningEvent(Context context, String sensorName, int temp, int batteryLevel) {
        if (context != null) {
            Intent intent = new Intent("huawei.intent.action.LOW_TEMP_WARNING_UI_EVENT");
            if (sensorName != null) {
                intent.putExtra("sensor_name", sensorName);
            }
            intent.putExtra("sensor_temp", temp);
            intent.putExtra("battery_level", batteryLevel);
            context.sendBroadcastAsUser(intent, UserHandle.ALL);
            Log.i("BroadcastAdapter", "send low temp warning event sensorname: " + sensorName + " temp =" + temp + ", batteryLevel = " + batteryLevel);
        }
    }

    public static void sendKeyThreadSchedEvent(Context context, String key, String value) {
        if (context != null && key != null) {
            Intent intent = new Intent("com.huawei.thermal.KEY_THREAD_SCHED");
            intent.putExtra(key, value);
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.thermal.receiverPermission", 0);
            Log.i("BroadcastAdapter", "sendBroadcast to iware," + key + " = " + value);
        }
    }
}
