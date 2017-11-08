package com.android.contacts.hap.numbermark;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.contacts.hap.service.NumberMarkInfo;
import com.google.android.gms.R;

public class NumberMarkUtil {
    private static boolean isW3Installed = false;
    private static String sAlreadyMark;
    private static String sAlreadyMarks;
    private static String sMarked;

    public static void clean() {
        sAlreadyMark = null;
        sAlreadyMarks = null;
        sMarked = null;
    }

    public static String getMarkLabel(Context context, NumberMarkInfo markInfo) {
        boolean z = true;
        if (context == null || markInfo == null || (markInfo.getClassify() == null && markInfo.getName() == null)) {
            return null;
        }
        if (TextUtils.isEmpty(markInfo.getName()) || TextUtils.isEmpty(markInfo.getClassify()) || !markInfo.isCloudMark()) {
            StringBuffer sb = new StringBuffer();
            if (sMarked == null) {
                sMarked = context.getString(R.string.marked);
            }
            if ("others".equals(markInfo.getClassify())) {
                if (markInfo.getName() == null) {
                    return null;
                }
                sb.append(markInfo.getName());
            } else if ("fraud".equals(markInfo.getClassify())) {
                sb.append(context.getString(R.string.number_mark_fraud));
            } else if ("crank".equals(markInfo.getClassify())) {
                sb.append(context.getString(R.string.number_mark_crank));
            } else if ("express".equals(markInfo.getClassify())) {
                sb.append(context.getString(R.string.number_mark_express));
            } else if ("house agent".equals(markInfo.getClassify())) {
                sb.append(context.getString(R.string.number_mark_house_agent));
            } else if (!"promote sales".equals(markInfo.getClassify())) {
                return null;
            } else {
                sb.append(context.getString(R.string.number_mark_promote_sales));
            }
            return String.format(sMarked, new Object[]{sb.toString()});
        } else if (markInfo.getMarkedCount() <= 0 && markInfo.getMarkedCount() != -501) {
            return markInfo.getName();
        } else {
            String classify = markInfo.getClassify();
            int markedCount = markInfo.getMarkedCount();
            if (markInfo.getMarkedCount() != -501) {
                z = false;
            }
            return appendMarkCount(context, classify, markedCount, z);
        }
    }

    public static String appendMarkCount(Context context, String classify, int markCount, boolean isPresentUpperLimitCount) {
        if (classify == null || context == null) {
            return "";
        }
        if (sAlreadyMark == null) {
            sAlreadyMark = context.getResources().getQuantityString(R.plurals.alreadymark_update, 1);
            sAlreadyMarks = context.getResources().getQuantityString(R.plurals.alreadymark_update, 2);
        }
        String markName = revertMarkTypeToMarkName(classify, context);
        if (TextUtils.isEmpty(markName)) {
            return markName;
        }
        if (markCount > 1) {
            return String.format(sAlreadyMarks, new Object[]{markName, Integer.valueOf(markCount)});
        } else if (isPresentUpperLimitCount) {
            return markName + context.getString(R.string.number_mark_present_upper_limit_str);
        } else {
            return String.format(sAlreadyMark, new Object[]{markName, Integer.valueOf(markCount)});
        }
    }

    public static int convertClassifyToType(String classify) {
        if ("fraud".equals(classify)) {
            return 1;
        }
        if ("crank".equals(classify)) {
            return 0;
        }
        if ("express".equals(classify)) {
            return 2;
        }
        if ("house agent".equals(classify)) {
            return 4;
        }
        if ("promote sales".equals(classify)) {
            return 3;
        }
        return 5;
    }

    public static Intent getIntentForMark(Context c, String number) {
        Intent intent = new Intent();
        intent.setAction("huawei.intent.action.MARK_PHONENUMBER");
        intent.putExtra("PHONE_NUMBER", number);
        intent.putExtra("MARK_SUMMERY", "");
        intent.putExtra("MARK_TYPE", -1);
        return intent;
    }

    public static boolean isW3AppInstalled() {
        return isW3Installed;
    }

    public static void setW3AppInstalled(boolean isInstalled) {
        isW3Installed = isInstalled;
    }

    public static boolean isUseNetwokMark(Context context) {
        if ("hw_numbermark_usenetworks".equals(System.getString(context.getContentResolver(), "hw_numbermark_option"))) {
            return true;
        }
        return false;
    }

    public static String revertMarkTypeToMarkName(String classify, Context context) {
        if (context == null) {
            return null;
        }
        if ("crank".equals(classify)) {
            return context.getString(R.string.number_mark_crank);
        }
        if ("fraud".equals(classify)) {
            return context.getString(R.string.number_mark_fraud);
        }
        if ("house agent".equals(classify)) {
            return context.getString(R.string.number_mark_house_agent);
        }
        if ("promote sales".equals(classify)) {
            return context.getString(R.string.number_mark_promote_sales);
        }
        if ("express".equals(classify)) {
            return context.getString(R.string.number_mark_express);
        }
        if ("taxi".equals(classify)) {
            return context.getString(R.string.number_mark_taxi);
        }
        return null;
    }
}
