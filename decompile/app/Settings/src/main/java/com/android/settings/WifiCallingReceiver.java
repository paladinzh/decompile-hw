package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.ims.ImsManager;

public class WifiCallingReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) {
            Log.d("WifiCallingReceiver", "check intent or context is null");
            return;
        }
        String action = intent.getAction();
        String type = intent.getType();
        Log.i("WifiCallingReceiver", "receive broadcast from OMACP and check " + action + " and " + type);
        if ("huawei.omacp.VOWIFI_CONTENT".equals(action) && "application/huawei.omacp.VOWIFI_CONTENT".equals(type)) {
            boolean isCheckBackgroud = intent.getBooleanExtra("vowifi", false);
            ImsManager.setWfcSetting(context, isCheckBackgroud);
            Log.i("WifiCallingReceiver", "set the switch " + isCheckBackgroud);
        }
    }
}
