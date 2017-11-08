package com.huawei.mms.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
import com.android.mms.transaction.SmsReceiverService;
import com.huawei.android.telephony.SmsInterceptionListenerEx;
import com.huawei.cspcommon.MLog;

public class VerifitionSmsInterceptionListener extends SmsInterceptionListenerEx {
    private Context mContext;

    public VerifitionSmsInterceptionListener(Context context) {
        this.mContext = context;
    }

    public int handleSmsDeliverAction(Bundle smsInfo) {
        MLog.d("VerifitionSmsInterceptionListener", "handleSmsDeliverAction: Receive a sms from listener");
        if (smsInfo == null) {
            MLog.e("VerifitionSmsInterceptionListener", "handleSmsDeliverAction: fail to get smsInfo");
            return -1;
        }
        Intent intent = (Intent) smsInfo.getParcelable("HANDLE_SMS_INTENT");
        if (intent != null) {
            return processRegistResponseSms(intent);
        }
        MLog.e("VerifitionSmsInterceptionListener", "handleSmsDeliverAction: fail to get RegistResponse sms intent");
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int processRegistResponseSms(Intent intent) {
        SmsMessage[] msgs = Intents.getMessagesFromIntent(intent);
        if (msgs == null || msgs.length <= 0 || !VerifitionSmsManager.getInstance().isVerifitionSms(msgs)) {
            return 0;
        }
        MLog.d("VerifitionSmsInterceptionListener", "processRegistResponseSms: start SmsReceiverService.");
        intent.setAction("com.android.mms.VERIFITION_SMS_ACTION");
        intent.putExtra("is_secret", 1);
        intent.setClass(this.mContext, SmsReceiverService.class);
        this.mContext.startService(intent);
        return 1;
    }
}
