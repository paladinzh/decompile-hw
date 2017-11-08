package com.huawei.systemmanager.sdk.tmsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TMSdkSecureReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()) && TMSEngineFeature.shouldInitTmsEngine()) {
                try {
                    new TMSdkSecureReceiverWrapper().doOnRecv(context, intent);
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (Error err) {
                    err.getStackTrace();
                }
            }
        }
    }
}
