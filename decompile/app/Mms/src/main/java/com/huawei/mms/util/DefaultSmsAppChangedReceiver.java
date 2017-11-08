package com.huawei.mms.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.Manifest.permission;
import com.huawei.cspcommon.MLog;

public class DefaultSmsAppChangedReceiver extends BroadcastReceiver {
    HwDefSmsAppChangedListener mDefSmsAppChangedListener;

    public interface HwDefSmsAppChangedListener {
        void onDefSmsAppChanged();
    }

    public DefaultSmsAppChangedReceiver(HwDefSmsAppChangedListener l) {
        this.mDefSmsAppChangedListener = l;
    }

    public void onReceive(Context context, Intent intent) {
        if (context.checkCallingPermission(permission.DEFAULTCHANGED_PERMISSION) == 0) {
            if (intent == null || !"com.huawei.mms.default_smsapp_changed".equals(intent.getAction())) {
                MLog.e("DefaultSmsAppChangedReceiver", "onReceive:: the broadcast is not valid!");
                return;
            }
            if (this.mDefSmsAppChangedListener != null) {
                this.mDefSmsAppChangedListener.onDefSmsAppChanged();
            }
        }
    }
}
