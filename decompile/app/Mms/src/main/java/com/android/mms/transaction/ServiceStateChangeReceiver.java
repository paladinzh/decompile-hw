package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.ServiceState;
import com.android.messaging.util.OsUtil;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwBackgroundLoader;

public class ServiceStateChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            if (OsUtil.isAtLeastL() && OsUtil.isSecondaryUser()) {
                MLog.w("Mms_TX_ServiceStateChangeReceiver", "ServiceStateChangeReceiver in SecondaryUser, Skipped");
                return;
            }
            if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                MLog.d("Mms_TX_ServiceStateChangeReceiver", "action: ACTION_SERVICE_STATE_CHANGED");
                if (HwBackgroundLoader.getBackgroundHandler().hasMessages(10001)) {
                    MLog.d("Mms_TX_ServiceStateChangeReceiver", "TransactionService has start ");
                    return;
                }
                ServiceState serviceState = ServiceState.newFromBundle(intent.getExtras());
                if (MLog.isLoggable("Mms_TXN", 2)) {
                    MLog.d("Mms_TX_ServiceStateChangeReceiver", "serviceState " + serviceState.getState());
                }
                if (serviceState.getState() == 0) {
                    MLog.d("Mms_TX_ServiceStateChangeReceiver", "in service, start TransactionService");
                    TransactionService.startMeDelayed(context, 5000);
                }
            }
        }
    }
}
