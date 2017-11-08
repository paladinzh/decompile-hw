package com.huawei.harassmentinterception.util;

import android.content.Context;
import android.content.Intent;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.systemmanager.util.HwLog;

public class SmsIntentHelper {
    private static final String SUBSCRIPTION_KEY = "subscription";
    private static final String TAG = "SmsIntentHelper";

    public static SmsMsgInfo getSmsInfoFromIntent(Context context, Intent intent) {
        if (intent != null) {
            return getSmsInfoFromSmsMessages(context, getMessagesFromIntent(context, intent), intent);
        }
        HwLog.e(TAG, "getSmsInfoFromIntent: Invalid intent");
        return null;
    }

    private static SmsMsgInfo getSmsInfoFromSmsMessages(Context context, SmsMessage[] messages, Intent intent) {
        int i = 0;
        if (messages == null || messages.length <= 0) {
            HwLog.w(TAG, "getSmsInfoFromSmsMessages : Invalid messages");
            return null;
        }
        String strMsgBody;
        String strPhoneNumber = messages[0].getOriginatingAddress();
        if (TextUtils.isEmpty(strPhoneNumber)) {
            HwLog.w(TAG, "getSmsInfoFromSmsMessages : Empty Phone number, " + strPhoneNumber);
            strPhoneNumber = "";
        }
        if (1 == messages.length) {
            strMsgBody = messages[0].getMessageBody();
        } else {
            StringBuilder body = new StringBuilder();
            int length = messages.length;
            while (i < length) {
                SmsMessage message = messages[i];
                if (message.mWrappedSmsMessage != null) {
                    body.append(message.getDisplayMessageBody());
                }
                i++;
            }
            strMsgBody = body.toString();
        }
        int subId = intent.getIntExtra(SUBSCRIPTION_KEY, -1);
        if (-1 == subId) {
            HwLog.w(TAG, "getSmsInfoFromSmsMessages: Fail to get subId from sms intent");
        }
        return new SmsMsgInfo(strPhoneNumber, "", strMsgBody, System.currentTimeMillis(), subId);
    }

    private static SmsMessage[] getMessagesFromIntent(Context context, Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        if (messages == null || messages.length <= 0) {
            HwLog.e(TAG, "getMessagesFromIntent: Can not get pdus info from intent");
            return new SmsMessage[0];
        }
        int i;
        String format = intent.getStringExtra("format");
        byte[][] pduObjs = new byte[messages.length][];
        for (i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i], format);
        }
        return msgs;
    }
}
