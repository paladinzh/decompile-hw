package com.huawei.harassmentinterception.strategy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.BlockCheckProvider;
import com.huawei.systemmanager.util.HwLog;

public class IntellEngineController {
    private static final String TAG = "IntellEngineController";
    private static final Uri block_sms_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.BlockCheckProvider"), BlockCheckProvider.BLOCK_SMS);

    public static void openSmsEngine(Context ctx) {
        HwLog.i(TAG, "openSmsEngine called");
        ctx.sendBroadcast(new Intent(ConstValues.ACTION_INTERCEPT_ENGINE_OPEN), "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    public static void closeSmsEngine(Context ctx) {
        ctx.sendBroadcast(new Intent(ConstValues.ACTION_INTERCEPT_ENGINE_CLOSE), "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
    }

    public static boolean handleSms(Context ctx, SmsIntentWrapper smsIntentWrapper) {
        try {
            Bundle bundle = new Bundle();
            bundle.putParcelable(ConstValues.KEY_SMS_PARAM, smsIntentWrapper.getSmsIntent());
            return ctx.getContentResolver().call(block_sms_uri, ConstValues.METHOD_ISBLOCKSMS, null, bundle).getBoolean(ConstValues.KEY_RESULT_IS_BLOCK);
        } catch (Exception ex) {
            HwLog.e(TAG, "handleSms" + ex.getMessage());
            return false;
        }
    }
}
