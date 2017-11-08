package com.android.contacts.hap.yellowpage;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.android.contacts.util.HwLog;
import com.huawei.yellowpage.YpSdkMgr;
import java.util.List;

public class YellowPageUtils {
    public static void initPlug(Context aContext) {
        if (aContext == null) {
            if (HwLog.HWFLOW) {
                HwLog.i("YellowPageUtils", "[YellowPageUtils] initPlug aContext is null");
            }
            return;
        }
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageUtils", "[YellowPageUtils] initPlug in");
        }
        if (isDisable(aContext, "com.huawei.yellowpage").booleanValue()) {
            enableApp(aContext, "com.huawei.yellowpage");
        }
        YpSdkMgr.getInstance().initPlug(aContext.getApplicationContext());
        if (HwLog.HWFLOW) {
            HwLog.i("YellowPageUtils", "[YellowPageUtils] initPlug out");
        }
    }

    public static boolean checkPackageInstall(Context aContext, String packageName) {
        long start = System.currentTimeMillis();
        if (TextUtils.isEmpty(packageName) || aContext == null) {
            if (HwLog.HWFLOW) {
                HwLog.i("YellowPageUtils", "[YellowPageUtils] checkPackageInstall input parameters is invalid");
            }
            return false;
        }
        try {
            if (aContext.getPackageManager().getApplicationInfo(packageName, 8192) != null) {
                if (HwLog.HWDBG) {
                    HwLog.d("YellowPageUtils", "[YellowPageUtils] checkPackageInstall coast1:" + (System.currentTimeMillis() - start));
                }
                return true;
            }
        } catch (NameNotFoundException e) {
            HwLog.w("YellowPageUtils", "[YellowPageUtils] checkPackageInstall fail, NameNotFoundException");
        }
        return false;
    }

    public static void enableApp(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            HwLog.w("YellowPageUtils", "[YellowPageUtils] enableApp input parameters is invalid");
            return;
        }
        try {
            context.getPackageManager().setApplicationEnabledSetting(packageName, 0, 0);
        } catch (Exception e) {
            HwLog.w("YellowPageUtils", "[YellowPageUtils] enableApp fail");
        }
    }

    public static Boolean isDisable(Context aContext, String packageName) {
        if (aContext == null || TextUtils.isEmpty(packageName)) {
            HwLog.w("YellowPageUtils", "[YellowPageUtils] isDisable input parameters is invalid");
            return Boolean.valueOf(false);
        }
        try {
            PackageManager mPm = aContext.getPackageManager();
            ApplicationInfo info = mPm.getApplicationInfo(packageName, 1152);
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            intent.setPackage(packageName);
            List<ResolveInfo> homes = mPm.queryIntentActivities(intent, 0);
            if (homes != null && homes.size() > 0 && mPm.checkSignatures(packageName, "android") >= 0) {
                return Boolean.valueOf(false);
            }
            if (info.enabled) {
                return Boolean.valueOf(false);
            }
            return Boolean.valueOf(true);
        } catch (NameNotFoundException e) {
            HwLog.w("YellowPageUtils", "[YellowPageUtils] isDisable package dont found");
            return Boolean.valueOf(false);
        }
    }
}
