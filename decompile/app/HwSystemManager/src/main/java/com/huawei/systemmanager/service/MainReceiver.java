package com.huawei.systemmanager.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.customize.HsmCfgIntentService;
import com.huawei.systemmanager.util.HwLog;

public class MainReceiver extends BroadcastReceiver {
    public static final String TAG = "MainReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                HwLog.i(TAG, "action is empty");
            } else {
                HwLog.i(TAG, "receive action:" + action);
            }
            if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(action)) {
                handleBootComplete(context);
            }
        }
    }

    private void handleBootComplete(Context context) {
        Intent intent = new Intent(context, MainService.class);
        intent.setAction("android.intent.action.BOOT_COMPLETED");
        context.startService(intent);
        Intent serviceIntent = new Intent("android.intent.action.BOOT_COMPLETED");
        serviceIntent.setClass(context, HsmCfgIntentService.class);
        context.startService(serviceIntent);
    }
}
