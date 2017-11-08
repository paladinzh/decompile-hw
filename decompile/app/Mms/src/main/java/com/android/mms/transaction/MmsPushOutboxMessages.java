package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.huawei.cspcommon.MLog;

public class MmsPushOutboxMessages extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (MLog.isLoggable("Mms_TXN", 2)) {
            MLog.v("Mms_TX_PushOutboxMessages", "Received the MMS_SEND_OUTBOX_MSG ");
        }
        if ("android.intent.action.MMS_SEND_OUTBOX_MSG".equalsIgnoreCase(intent.getAction())) {
            MLog.d("Mms_TX_PushOutboxMessages", "Now waking up the MMS service");
            TransactionService.startMe(context);
        }
    }
}
