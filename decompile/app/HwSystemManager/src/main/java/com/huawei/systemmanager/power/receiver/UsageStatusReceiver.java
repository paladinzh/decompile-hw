package com.huawei.systemmanager.power.receiver;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.power.comm.ApplicationConstant;
import com.huawei.systemmanager.power.model.UsageStatusHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class UsageStatusReceiver extends HsmBroadcastReceiver {
    private static final String TAG = "UsageStatusReceiver";

    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                HwLog.w(TAG, "onReceive : Invalid  action");
                return;
            }
            HwLog.i(TAG, "onReceive: Action = " + action);
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        if (ApplicationConstant.ACTION_ALARM_UPDATE_USAGESTATUS_RECORD.equals(intent.getAction())) {
            UsageStatusHelper.recordBaseScreenRatio();
            UsageStatusHelper.deleteLongestUsageInfo();
        }
    }
}
