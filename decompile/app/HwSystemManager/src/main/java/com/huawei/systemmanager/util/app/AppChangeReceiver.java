package com.huawei.systemmanager.util.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class AppChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
            HsmPackageManager.getInstance().onReceive(context, intent);
        }
    }
}
