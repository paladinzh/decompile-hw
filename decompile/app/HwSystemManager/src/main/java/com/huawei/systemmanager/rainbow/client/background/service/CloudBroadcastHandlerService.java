package com.huawei.systemmanager.rainbow.client.background.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;

public class CloudBroadcastHandlerService implements HsmService {
    public static final String TAG = "CloudBroadcastHandlerService";
    private static BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!CloudSwitchHelper.isCloudEnabled()) {
                HwLog.e(CloudBroadcastHandlerService.TAG, "CloudBroadcastHandlerService the rainbow is not enabled!");
            } else if (intent == null) {
                HwLog.e(CloudBroadcastHandlerService.TAG, "intent is null");
            } else {
                String action = intent.getAction();
                HwLog.d(CloudBroadcastHandlerService.TAG, "action = " + action);
                String bootCompleted = "android.intent.action.BOOT_COMPLETED";
                if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    Intent serviceIntent = new Intent("android.net.conn.CONNECTIVITY_CHANGE");
                    serviceIntent.setClass(context, RainbowCommonService.class);
                    context.startService(serviceIntent);
                } else if (bootCompleted.equals(action)) {
                    Utility.updateRegistedKey(context);
                }
            }
        }
    };
    private Context mContext = null;
    private boolean mRegistered = false;

    public CloudBroadcastHandlerService(Context context) {
        this.mContext = context;
    }

    public void init() {
        if (this.mRegistered) {
            HwLog.d(TAG, "already register!");
            return;
        }
        HwLog.d(TAG, "register broadcast!");
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(mBroadcastReceiver, filter);
        this.mRegistered = true;
    }

    public void onDestroy() {
        if (this.mRegistered) {
            this.mContext.unregisterReceiver(mBroadcastReceiver);
            this.mRegistered = false;
            HwLog.d(TAG, "unregister broadcast!");
        }
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }
}
