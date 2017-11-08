package com.huawei.systemmanager.optimize.base;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class Utility {
    public static String replaceBlank(String beforeDeleteStr) {
        String afterDeleteStr = "";
        if (beforeDeleteStr != null) {
            return beforeDeleteStr.replaceAll("\\s", " ").trim();
        }
        return afterDeleteStr;
    }

    public static boolean isSystemApp(Context context, String packageName) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(packageName, 8192);
            return ((appInfo.flags & 128) == 0 && (appInfo.flags & 1) == 0) ? false : true;
        } catch (NameNotFoundException e) {
            HwLog.i(Utility.class.getSimpleName(), "isSystemApp,can not find pkg:" + packageName);
        }
    }

    public static ArrayList<String> getAllUserApp(Context context) {
        List<ApplicationInfo> appList = context.getPackageManager().getInstalledApplications(8192);
        ArrayList<String> thirdAppNameList = new ArrayList();
        for (ApplicationInfo app : appList) {
            if ((app.flags & 1) == 0) {
                thirdAppNameList.add(app.packageName);
            } else if ((app.flags & 128) != 0) {
                thirdAppNameList.add(app.packageName);
            }
        }
        return thirdAppNameList;
    }

    public static BitmapDrawable getAppLogo(String packageName, Context context) {
        BitmapDrawable icon = null;
        try {
            return (BitmapDrawable) context.getPackageManager().getApplicationIcon(packageName);
        } catch (NameNotFoundException e1) {
            e1.printStackTrace();
            return icon;
        }
    }

    public static String getAppName(String packageName, Context context) {
        ApplicationInfo appInfo = null;
        PackageManager manager = context.getPackageManager();
        try {
            appInfo = manager.getApplicationInfo(packageName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (appInfo != null) {
            return replaceBlank(manager.getApplicationLabel(appInfo).toString());
        }
        return null;
    }
}
