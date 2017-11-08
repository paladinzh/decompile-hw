package com.android.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemProperties;

public class HwCustKeyguardUpdateMonitorImpl extends HwCustKeyguardUpdateMonitor {
    private static final String ACTION_SIM_ICCID_READY = "android.intent.action.ACTION_SIM_ICCID_READY";
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (HwCustKeyguardUpdateMonitorImpl.ACTION_SIM_ICCID_READY.equals(intent.getAction())) {
                HwCustKeyguardUpdateMonitorImpl.this.setCustLanguage(context);
            }
        }
    };
    private boolean mEnabled = "true".equals(SystemProperties.get("ro.config.iccid_language", "false"));

    public void setCustLanguage(Context context) {
        if (this.mEnabled && IccidConfig.isCustIccid(context)) {
            IccidConfig.init();
            IccidConfig.setCustLanguage();
        }
    }

    public void registerHwReceiver(Context context) {
        if (this.mEnabled) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_SIM_ICCID_READY);
            context.registerReceiver(this.mBroadcastReceiver, filter);
        }
    }
}
