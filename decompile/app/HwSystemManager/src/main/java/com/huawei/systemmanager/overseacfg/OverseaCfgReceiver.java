package com.huawei.systemmanager.overseacfg;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.customize.OverseaCfgConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class OverseaCfgReceiver extends HsmBroadcastReceiver {
    private static final String LOG_TAG = "OverseaCfgReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            HwLog.w(LOG_TAG, "onReceive : Invalid context or intent");
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            HwLog.w(LOG_TAG, "onReceive : Invalid intent action");
            return;
        }
        HwLog.d(LOG_TAG, "onReceive action = " + action);
        sendToBackground(context.getApplicationContext(), intent);
    }

    public void doInBackground(Context context, Intent intent) {
        super.doInBackground(context, intent);
        HwLog.d(LOG_TAG, "doInBackground setOverseaPermission start!");
        if (intent != null && OverseaCfgConst.DO_OVERSEA_SWITCH_OPEN_ACTION.equals(intent.getAction())) {
            OverseaCfgHelper.setOverseaSwitchChange(context, intent);
        }
    }
}
