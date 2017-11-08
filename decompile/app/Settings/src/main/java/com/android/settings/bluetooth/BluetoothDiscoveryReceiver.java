package com.android.settings.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.settings.MLog;

public final class BluetoothDiscoveryReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            MLog.e("BluetoothDiscoveryReceiver", "content of intent is illeagal");
            return;
        }
        String action = intent.getAction();
        Log.v("BluetoothDiscoveryReceiver", "Received: " + action);
        if (action.equals("android.bluetooth.adapter.action.DISCOVERY_STARTED") || action.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
            LocalBluetoothPreferences.persistDiscoveringTimestamp(context);
        }
    }
}
