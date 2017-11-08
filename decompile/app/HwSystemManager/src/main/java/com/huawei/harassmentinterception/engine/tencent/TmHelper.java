package com.huawei.harassmentinterception.engine.tencent;

import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.systemmanager.util.HwLog;
import tmsdk.common.SmsEntity;
import tmsdk.common.module.intelli_sms.IntelliSmsCheckResult;

public class TmHelper {
    private static final String TAG = "TmHelper";

    public static SmsEntity getSmsEntity(SmsIntentWrapper smsIntentWrapper) {
        SmsMsgInfo smsInfo = smsIntentWrapper.getSmsMsgInfo();
        SmsEntity smsEntity = new SmsEntity();
        smsEntity.phonenum = smsInfo.getPhone();
        smsEntity.body = smsInfo.getBody();
        return smsEntity;
    }

    public static boolean parseSmsCheckResult(IntelliSmsCheckResult checkResult) {
        switch (checkResult.suggestion) {
            case 1:
                HwLog.i(TAG, "parseSmsCheckResult: SUGGESTION_PASS");
                return false;
            case 2:
            case 3:
                HwLog.i(TAG, "parseSmsCheckResult: INTERCEPT or DOUBT , suggestion = " + checkResult.suggestion);
                return true;
            default:
                HwLog.w(TAG, "parseSmsCheckResult: Unknown suggestion = " + checkResult.suggestion);
                return false;
        }
    }
}
