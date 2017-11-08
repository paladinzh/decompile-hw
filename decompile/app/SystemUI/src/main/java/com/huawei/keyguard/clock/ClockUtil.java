package com.huawei.keyguard.clock;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import com.android.huawei.keyguard.keyguardplus.HwCustKeyguardStatusViewEx;
import com.android.keyguard.R$array;
import com.huawei.cust.HwCustUtils;
import com.huawei.keyguard.util.HwLog;

public class ClockUtil {
    private static HwCustKeyguardStatusViewEx mCustKeyguardStatusViewEx = ((HwCustKeyguardStatusViewEx) HwCustUtils.createObj(HwCustKeyguardStatusViewEx.class, new Object[0]));

    public static String formatChinaDateTime(Context context, String normalTime) {
        if (context == null || normalTime == null) {
            HwLog.w("ClockUtil", "The parameter is null!");
            return normalTime;
        }
        Resources resources = context.getResources();
        if (resources == null) {
            HwLog.w("ClockUtil", "The resources is null!");
            return normalTime;
        }
        String[] normal12Time = resources.getStringArray(R$array.normal_12_time);
        String[] chinaTime = resources.getStringArray(R$array.china_time);
        if (normal12Time == null || chinaTime == null) {
            return normalTime;
        }
        int normal12TimeLength = normal12Time.length;
        int chinaTimeLength = chinaTime.length;
        if (normal12TimeLength == 0 || chinaTimeLength == 0 || chinaTimeLength != normal12TimeLength) {
            return normalTime;
        }
        for (int i = 0; i < normal12TimeLength; i++) {
            if (normalTime.contains(normal12Time[i])) {
                normalTime = normalTime.replace(normal12Time[i], chinaTime[i]);
                break;
            }
        }
        return normalTime;
    }

    public static String getFormatChinaDateTimeAmpm(Context context, String ampmTime, String postFix) {
        String timeString = formatChinaDateTime(context, ampmTime + postFix);
        if (!TextUtils.isEmpty(timeString)) {
            return timeString.substring(0, timeString.indexOf(postFix));
        }
        HwLog.w("ClockUtil", "The timeString is invalid!");
        return ampmTime;
    }

    public static boolean isShowFullMonth() {
        return mCustKeyguardStatusViewEx != null ? mCustKeyguardStatusViewEx.isShowFullMonth() : false;
    }

    public static boolean isShowFrenchCustDate(Context context) {
        return mCustKeyguardStatusViewEx != null ? mCustKeyguardStatusViewEx.isShowFrenchCustDate(context) : false;
    }

    private static int findFirstDigit(String timeStr, int len) {
        for (int i = 0; i < len; i++) {
            if (Character.isDigit(timeStr.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static int findLastDigit(String timeStr, int len) {
        int index = -1;
        for (int i = 0; i < len; i++) {
            if (Character.isDigit(timeStr.charAt(i))) {
                index = i;
            }
        }
        return index;
    }

    private static String revertAmpmStr(String timeStr, int ds, int de, int as, int ae) {
        String digitStr = timeStr.substring(ds, de);
        return digitStr + timeStr.substring(as, ae);
    }

    public static SpannableString setTimeStr(String timeStr) {
        boolean needFixAmpm = false;
        int ampmStart = 0;
        int ampmEnd = 0;
        int timelen = timeStr.length();
        int digitStart = findFirstDigit(timeStr, timelen);
        int digitEnd = findLastDigit(timeStr, timelen) + 1;
        if (digitStart == 0) {
            ampmStart = digitEnd;
            ampmEnd = timelen;
        }
        if (digitEnd == timelen) {
            ampmEnd = digitStart;
        }
        int apmpmlen = ampmEnd - ampmStart;
        if (apmpmlen <= 0) {
            return new SpannableString(timeStr);
        }
        if (digitStart == ampmEnd) {
            needFixAmpm = true;
        }
        String amendStr = timeStr;
        if (needFixAmpm) {
            amendStr = revertAmpmStr(timeStr, digitStart, digitEnd, ampmStart, ampmEnd);
        }
        SpannableString msp = new SpannableString(amendStr);
        msp.setSpan(new AbsoluteSizeSpan(12, true), timelen - apmpmlen, timelen, 33);
        return msp;
    }
}
