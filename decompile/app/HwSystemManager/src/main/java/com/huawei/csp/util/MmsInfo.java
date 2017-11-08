package com.huawei.csp.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MmsInfo {
    private static final String CONTACT_PACKAGE_NAME = "com.android.contacts";
    public static final String MMS_ACTIVITY_CL = "com.android.mms.ui.ConversationList";
    public static final String MMS_ACTIVITY_CMA = "com.android.mms.ui.ComposeMessageActivity";
    private static final String MMS_PACKAGE_NAME = "com.android.mms";
    private static final String TAG = "MmsInfo";
    private static boolean mInCspMode = false;
    private static String mSmsPackageName = null;

    private static void initCspSettings(Context context) {
        boolean z = false;
        Intent intent = new Intent();
        intent.setClassName("com.android.contacts", MMS_ACTIVITY_CMA);
        if (context.getPackageManager().resolveActivity(intent, 0) != null) {
            z = true;
        }
        mInCspMode = z;
        mSmsPackageName = mInCspMode ? "com.android.contacts" : MMS_PACKAGE_NAME;
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
                activityIcon = pm.getActivityIcon(new ComponentName(smsPackageName, MMS_ACTIVITY_CL));
            } else {
                activityIcon = pm.getApplicationIcon(smsPackageName);
            }
            return activityIcon;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getSmsAppIcon fail" + e.getMessage());
            Log.e(TAG, "getSmsAppIcon fail");
            return null;
        }
    }

    public static final String getSmsAppLabel(Context context) {
        String str = null;
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        try {
            ActivityInfo act = pm.getActivityInfo(new ComponentName(getSmsAppName(context), MMS_ACTIVITY_CL), 128);
            if (act != null) {
                str = act.loadLabel(pm).toString();
            }
            return str;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getSmsAppIcon fail" + e.getMessage());
            Log.e(TAG, "getSmsAppLabel fail");
            return null;
        }
    }
}
