package com.huawei.powergenie.integration.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;
import java.util.ArrayList;

public final class BroadcastAdapter {
    private BroadcastAdapter() {
    }

    public static void sendWasteBatteryApp(Context context, String appName, long wakelockTime, int wakeupNum, int wakeupInterval, long date, String reason, ArrayList<String> pkgList, ArrayList<String> reasonList) {
        if (context == null || appName == null || pkgList == null || reasonList == null || pkgList.size() != reasonList.size()) {
            Log.w("BroadcastAdapter", "not send high power app to HSM because invalid args");
            return;
        }
        int reasonType = transHighPowerReasonType(reason);
        ArrayList<Integer> reasonTypeList = new ArrayList();
        for (String reasonItem : reasonList) {
            reasonTypeList.add(Integer.valueOf(transHighPowerReasonType(reasonItem)));
        }
        Intent intent = new Intent("huawei.intent.action.PG_FOUND_WASTE_POWER_APP");
        intent.putExtra("pkgName", appName);
        intent.putExtra("wakelockTime", wakelockTime);
        intent.putExtra("wakeupNum", wakeupNum);
        intent.putExtra("wakeupInterval", wakeupInterval);
        intent.putExtra("date", date);
        intent.putExtra("reason", reasonType);
        intent.putStringArrayListExtra("pkgList", pkgList);
        intent.putIntegerArrayListExtra("reasonList", reasonTypeList);
        context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
        Log.i("BroadcastAdapter", "send " + pkgList.size() + " high power app to HSM");
    }

    public static void sendHighPowerCleanApp(Context context, String pkgName) {
        if (context != null && pkgName != null) {
            long timeStamp = System.currentTimeMillis();
            Intent intent = new Intent("com.huawei.intent.action.PG_CLEAN_POWER_CONSUME_APP");
            intent.putExtra("pkgName", pkgName);
            intent.putExtra("timestamp", timeStamp);
            context.sendBroadcastAsUser(intent, UserHandle.CURRENT, "com.huawei.powergenie.receiverPermission", 0);
            Log.i("BroadcastAdapter", "send a high power clean app to sm: " + pkgName + ", time : " + timeStamp);
        }
    }

    public static void sendThermalUIEvent(Context context, String event) {
        if (context != null && event != null) {
            Intent intent = new Intent("huawei.intent.action.THERMAL_UI_EVENT");
            intent.putExtra("Event", event);
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
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
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
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
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
            Log.i("BroadcastAdapter", "send low temp warning event sensorname: " + sensorName + " temp =" + temp + ", batteryLevel = " + batteryLevel);
        }
    }

    public static void sendFeatureEnable(Context context, boolean enable) {
        if (context != null) {
            Intent intent = new Intent("huawei.intent.action.PG_EXTREME_ENABLE_FEATURE_ACTION");
            intent.putExtra("enable", enable);
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
            Log.i("BroadcastAdapter", "send Feature Enable = " + enable);
        }
    }

    public static void queryBtActiveApps(Context context) {
        if (context != null) {
            context.sendBroadcastAsUser(new Intent("com.huawei.intent.action.PG_QUERY_BT_ACTIVE_APPS"), UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
            Log.i("BroadcastAdapter", "send requestion to query bt active apps!");
        }
    }

    public static void startIAware(Context context) {
        if (context != null) {
            Intent intent = new Intent("com.huawei.intent.action.PG_START_IAWARE");
            intent.setPackage("com.huawei.iaware");
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
            Log.i("BroadcastAdapter", "send start broadcast to iaware!");
        }
    }

    public static void startThermal(Context context) {
        if (context != null) {
            Intent intent = new Intent("huawei.intent.action.START_THERMAL_ACTION");
            intent.setPackage("com.huawei.powergenie");
            context.sendBroadcastAsUser(intent, UserHandle.ALL, "com.huawei.powergenie.receiverPermission", 0);
            Log.i("BroadcastAdapter", "send start broadcast to thermal!");
        }
    }

    private static int transHighPowerReasonType(String reason) {
        if ("alarm".equals(reason)) {
            return 0;
        }
        if ("wakelock".equals(reason) || "crash".equals(reason)) {
            return 1;
        }
        if ("gps".equals(reason)) {
            return 2;
        }
        return -1;
    }
}
