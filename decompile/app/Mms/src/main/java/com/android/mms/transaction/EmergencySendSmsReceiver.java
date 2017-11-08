package com.android.mms.transaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class EmergencySendSmsReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Log.d("EmergencySendSmsReceiver", "onReceive EMERGENCY_SEND_SMS");
        boolean isRoaming = false;
        boolean isUKnetwork = false;
        String roaming = SystemProperties.get("gsm.operator.isroaming", null);
        if (!TextUtils.isEmpty(roaming)) {
            isRoaming = "true".equals(roaming);
        }
        String mcc = getMcc(context);
        if (!TextUtils.isEmpty(mcc) && mcc.equals("234")) {
            isUKnetwork = true;
        }
        if (!isRoaming && isUKnetwork) {
            context.startService(new Intent(context, SendSmsService.class));
        }
    }

    private String getMcc(Context context) {
        String operator = ((TelephonyManager) context.getSystemService("phone")).getNetworkOperator();
        if (TextUtils.isEmpty(operator)) {
            return null;
        }
        return operator.substring(0, 3);
    }
}
