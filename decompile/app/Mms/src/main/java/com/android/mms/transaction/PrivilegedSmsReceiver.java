package com.android.mms.transaction;

import android.content.Context;
import android.content.Intent;
import com.huawei.cspcommon.MLog;

public class PrivilegedSmsReceiver extends SmsReceiver {
    public void onReceive(Context context, Intent intent) {
        MLog.w("Mms_TXS_PrivReceiver", "intent: ***");
        onReceiveWithPrivilege(context, intent, true);
    }
}
