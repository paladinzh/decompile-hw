package com.huawei.keyguard.policy;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import com.android.keyguard.KeyguardSecurityModel.SecurityMode;
import com.android.keyguard.R$plurals;
import com.android.keyguard.R$string;
import com.huawei.keyguard.policy.RetryPolicy.IRetryPolicy;
import com.huawei.timekeeper.TimeTickInfo;
import fyusion.vislib.BuildConfig;

public class ErrorMessage {
    public static String getFingerFobiddenMessage(Context context, boolean isPswdLock) {
        String time = BuildConfig.FLAVOR;
        Resources r = context.getResources();
        if (isPswdLock) {
            IRetryPolicy policy = RetryPolicy.getDefaultPolicy(context);
            if (policy.getRemainingChance() <= 0) {
                time = getTimeStr(context, policy.getTimeTickInfo());
            }
        } else {
            int fingerRemaing = (int) FingerBlackCounter.getRemainingTime();
            IRetryPolicy fingerPolicy = RetryPolicy.getFingerPolicy(context);
            if (fingerRemaing <= 0 && fingerPolicy.getRemainingChance() <= 0) {
                fingerRemaing = (int) fingerPolicy.getRemainingTime();
            }
            fingerRemaing = (fingerRemaing + 499) / 1000;
            if (fingerRemaing > 0) {
                time = r.getQuantityString(R$plurals.kg_time_second, fingerRemaing, new Object[]{Integer.valueOf(fingerRemaing)});
            }
        }
        if (TextUtils.isEmpty(time)) {
            return time;
        }
        return r.getString(R$string.kg_fingerprint_suspended, new Object[]{time});
    }

    public static String getTimeStr(Context context, TimeTickInfo ti) {
        int i;
        if (ti.getHour() > 0) {
            if (ti.getMinute() > 30) {
                i = 1;
            } else {
                i = 0;
            }
            int hour = i + ti.getHour();
            return context.getResources().getQuantityString(R$plurals.kg_time_hour, hour, new Object[]{Integer.valueOf(hour)});
        } else if (ti.getMinute() > 0) {
            if (ti.getSecond() > 30) {
                i = 1;
            } else {
                i = 0;
            }
            int minute = i + ti.getMinute();
            return context.getResources().getQuantityString(R$plurals.kg_time_minute, minute, new Object[]{Integer.valueOf(minute)});
        } else if (ti.getSecond() <= 0) {
            return BuildConfig.FLAVOR;
        } else {
            return context.getResources().getQuantityString(R$plurals.kg_time_second, ti.getSecond(), new Object[]{Integer.valueOf(ti.getSecond())});
        }
    }

    public static String getTimeoutMessage(Context context, SecurityMode securityMode, TimeTickInfo ti) {
        if (TextUtils.isEmpty(getTimeStr(context, ti))) {
            return null;
        }
        return context.getString(R$string.kg_verify_timeout_message, new Object[]{time});
    }
}
