package com.android.settingslib.bluetooth;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothDiscoverableTimeoutReceiver extends BroadcastReceiver {
    public static void setDiscoverableAlarm(Context context, long alarmTime) {
        HwLog.d("BluetoothDiscoverableTimeoutReceiver", "setDiscoverableAlarm(): alarmTime = " + alarmTime);
        Intent intent = new Intent("android.bluetooth.intent.DISCOVERABLE_TIMEOUT");
        intent.setClass(context, BluetoothDiscoverableTimeoutReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService("alarm");
        if (pending != null) {
            alarmManager.cancel(pending);
            Log.d("BluetoothDiscoverableTimeoutReceiver", "setDiscoverableAlarm(): cancel prev alarm");
        }
        alarmManager.setExact(0, alarmTime, PendingIntent.getBroadcast(context, 0, intent, 0));
    }

    public static void cancelDiscoverableAlarm(Context context) {
        HwLog.d("BluetoothDiscoverableTimeoutReceiver", "cancelDiscoverableAlarm(): Enter");
        Intent intent = new Intent("android.bluetooth.intent.DISCOVERABLE_TIMEOUT");
        intent.setClass(context, BluetoothDiscoverableTimeoutReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, 536870912);
        if (pending != null) {
            ((AlarmManager) context.getSystemService("alarm")).cancel(pending);
        }
    }

    public void onReceive(Context context, Intent intent) {
        LocalBluetoothAdapter localBluetoothAdapter = LocalBluetoothAdapter.getInstance();
        if (localBluetoothAdapter == null || localBluetoothAdapter.getState() != 12) {
            HwLog.e("BluetoothDiscoverableTimeoutReceiver", "localBluetoothAdapter is NULL!!");
            return;
        }
        HwLog.d("BluetoothDiscoverableTimeoutReceiver", "Disable discoverable...");
        localBluetoothAdapter.setScanMode(21);
    }
}
