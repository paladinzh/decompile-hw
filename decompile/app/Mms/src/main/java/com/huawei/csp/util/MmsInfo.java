package com.huawei.csp.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MmsInfo {
    private static boolean mInCspMode = false;
    private static String mSmsPackageName = null;

    private static void initCspSettings(Context context) {
        boolean z = false;
        Intent intent = new Intent();
        intent.setClassName("com.android.contacts", "com.android.mms.ui.ComposeMessageActivity");
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
            z = true;
        }
        mInCspMode = z;
        mSmsPackageName = mInCspMode ? "com.android.contacts" : "com.android.mms";
    }

    public static String getSmsAppName(Context context) {
        if (mSmsPackageName == null) {
            initCspSettings(context);
        }
        return mSmsPackageName;
    }

    public static boolean isInCspMode(Context context) {
        if (mSmsPackageName == null) {
            initCspSettings(context);
        }
        return mInCspMode;
    }

    public static final Drawable getSmsAppIcon(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        try {
            Drawable activityIcon;
            String smsPackageName = getSmsAppName(context);
            if (mInCspMode) {
                activityIcon = pm.getActivityIcon(new ComponentName(smsPackageName, "com.android.mms.ui.ConversationList"));
            } else {
                activityIcon = pm.getApplicationIcon(smsPackageName);
            }
            return activityIcon;
        } catch (NameNotFoundException e) {
            Log.e("MmsInfo", "getSmsAppIcon fail" + e.getMessage());
            Log.e("MmsInfo", "getSmsAppIcon fail");
            return null;
        }
    }
}
