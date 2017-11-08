package com.android.settings.applications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class BlackListReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            Intent serviceIntent = new Intent().setClassName("com.android.settings", "com.android.settings.applications.BlackListService");
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                serviceIntent.putExtra("action_type", "android.intent.action.BOOT_COMPLETED");
                context.startService(serviceIntent);
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_REPLACED".equals(action)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                if (!TextUtils.isEmpty(packageName)) {
                    serviceIntent.putExtra("action_type", action);
                    serviceIntent.putExtra("package_name", packageName);
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
