package com.android.settings;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;

public class HwCustPolicyTimeoutReceiver extends BroadcastReceiver {
    private static final long FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;
    private static final String SCREENLOCK_EXCHANGE_TIMEOUT_ACTION = "com.huawei.exchange.CHANGE_SCREEN_LOCK_AFTER_TIMEOUT";
    private static final long[] SCREEN_TIMEOUT_VALUES = new long[]{15000, FALLBACK_SCREEN_TIMEOUT_VALUE, 60000, 120000, 300000, 600000, 1800000};
    private static final String TAG = "HwCustPolicyTimeoutReceiver";
    private static final boolean USEING_ATT_EXCHANGE_POLICY = SystemProperties.getBoolean("hw_exchange_security_policy", false);

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            DevicePolicyManager mDPM = (DevicePolicyManager) context.getSystemService("device_policy");
            if (USEING_ATT_EXCHANGE_POLICY && SCREENLOCK_EXCHANGE_TIMEOUT_ACTION.equals(action)) {
                Log.d(TAG, "receive CHANGE_SCREEN_LOCK_AFTER_TIMEOUT action!");
                long adminTimeout = mDPM != null ? mDPM.getMaximumTimeToLock(null) : 0;
                if (adminTimeout > 0) {
                    long currentTimeout = System.getLong(context.getContentResolver(), "screen_off_timeout", FALLBACK_SCREEN_TIMEOUT_VALUE);
                    long nearMaxtimeout = FALLBACK_SCREEN_TIMEOUT_VALUE;
                    for (long i : SCREEN_TIMEOUT_VALUES) {
                        if (i <= adminTimeout) {
                            nearMaxtimeout = i;
                        }
                    }
                    if (currentTimeout > adminTimeout) {
                        System.putLong(context.getContentResolver(), "screen_off_timeout", nearMaxtimeout);
                    }
                    Secure.putLong(context.getContentResolver(), "lock_screen_lock_after_timeout", adminTimeout);
                }
            }
        }
    }
}
