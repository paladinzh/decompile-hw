package com.huawei.harassmentinterception.util;

import android.content.Context;
import android.content.Intent;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.systemmanager.util.HwLog;

public class SmsBlackListHelper {
    public static final String TAG = "SmsBlackListHelper";

    public static int handleSmsByBlackList(Context context, Intent intent) {
        return handleSmsByBlackList(context, SmsIntentHelper.getSmsInfoFromIntent(context, intent));
    }

    public static int handleIncomingCallByBlacklist(Context context, String strPhoneNumber) {
        if (DBAdapter.checkMatchBlacklist(context, strPhoneNumber, 2) == 0) {
            HwLog.i(TAG, "handleIncomingCallByBlacklist: Blacklist number, should be blocked");
            return 1;
        }
        HwLog.i(TAG, "handleIncomingCallByBlacklist: Not in blacklist or option doesn't match, pass");
        return 0;
    }

    public static int handleSmsByBlackList(Context context, SmsMsgInfo smsInfo) {
        if (smsInfo == null) {
            HwLog.e(TAG, "handleSms : Fail to get sms info from intent");
            return -1;
        } else if (DBAdapter.checkMatchBlacklist(context, smsInfo.getPhone(), 1) != 0) {
            HwLog.d(TAG, "handleSms: Not in blacklist or option doesn't match, pass");
            return 0;
        } else {
            HwLog.i(TAG, "handleSms: The sms should be blocked");
            return 1;
        }
    }
}
