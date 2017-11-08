package com.android.settings.applications;

import android.app.AppGlobals;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;

class ApplicationExtUtils {
    ApplicationExtUtils() {
    }

    public static boolean isPackageInfoExist(String tag, String packageName, int uid) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        PackageInfo packageInfo = null;
        try {
            packageInfo = AppGlobals.getPackageManager().getPackageInfo(packageName, 4096, UserHandle.getUserId(uid));
        } catch (RemoteException e) {
            Log.w(tag, "PackageManager is dead. Can't get package info " + packageName, e);
        }
        if (packageInfo != null) {
            return true;
        }
        Log.e(tag, packageName + " does not exist, it has been uninstalled!");
        return false;
    }

    public static void registerBlackListSharePreferenceListener(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
        if (applications != null) {
            new ApplicationPlatformImp().registerBlackListSharePreferenceListener(applications, filterAdapter);
        }
    }

    public static void unregisterBlackListSharePreferenceListener() {
        new ApplicationPlatformImp().unregisterBlackListSharePreferenceListener();
    }

    public static void setShowingBlackListAppFlagIfNeeded(Context context, Intent intent) {
        if (context != null && intent != null) {
            new ApplicationPlatformImp().setShowingBlackListAppFlagIfNeeded(context, intent);
        }
    }

    public static void changeDisableTextViewIfNeeded(String packageName, TextView disabled) {
        if (disabled != null && !TextUtils.isEmpty(packageName)) {
            new ApplicationPlatformImp().changeDisableTextViewIfNeeded(packageName, disabled);
        }
    }

    public static void selectForbiddenFilterIfNeeded(ManageApplications applications, Spinner filterSpinner, ArrayList<Integer> filterOptions) {
        if (applications != null && filterOptions != null && filterOptions.size() != 0) {
            new ApplicationPlatformImp().selectForbiddenFilterIfNeeded(applications, filterSpinner, filterOptions);
        }
    }

    public static void setForbiddenFilterEnabledIfNeeded(ManageApplications applications, FilterSpinnerAdapter filterAdapter) {
        if (applications != null) {
            new ApplicationPlatformImp().setForbiddenFilterEnabledIfNeeded(applications, filterAdapter);
        }
    }

    public static void showWarningDialog(Context context, ApplicationInfo info) {
        if (context != null && info != null) {
            new ApplicationPlatformImp().showWarningDialog(context, info);
        }
    }

    public static void dismissWarningDialog() {
        new ApplicationPlatformImp().dismissWarningDialog();
    }

    public static void setShowingBlackListAppFlag(boolean state) {
        new ApplicationPlatformImp().setShowingBlackListAppFlag(state);
    }

    public static boolean getShowingBlackListAppFlag() {
        return new ApplicationPlatformImp().getShowingBlackListAppFlag();
    }
}
