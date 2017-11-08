package com.android.server.rms.test;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.android.server.rms.record.AppUsageTime;
import com.android.server.rms.utils.Utils;

public class TestAppUsageTime {
    private static final String TAG = "RMS.TestAppUsageTime";
    private static final AppUsageTime mTest = AppUsageTime.self();

    private static final boolean checkInputArgs(Context context, String[] args) {
        if (args.length != 2 || args[1] == null) {
            Log.e(TAG, "please input correct package name for AppUsageTime test!");
            return false;
        }
        try {
            if (context.getPackageManager().getApplicationInfo(args[1], 0) != null) {
                return true;
            }
            Log.e(TAG, "There is not such an application, please check the package name carefully!!!");
            return false;
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getApplicationInfo failed. Please check the package name carefully!!!");
            return false;
        }
    }

    public static final void testGetUsageTime(Context context, String[] args) {
        if (checkInputArgs(context, args)) {
            String pkg = args[1];
            long time = mTest.getRealUsageTimeLocked(pkg) / 1000;
            if (Utils.HWFLOW) {
                Log.i(TAG, "pkg " + pkg + ", usageTime is " + time + "s");
            }
        }
    }

    public static final void testIsHistoryInstalledApp(Context context, String[] args) {
        if (checkInputArgs(context, args)) {
            String pkg = args[1];
            if (mTest.isHistoryInstalledApp(pkg)) {
                if (Utils.HWFLOW) {
                    Log.i(TAG, "pkg " + pkg + " is a history installed application");
                }
            } else if (Utils.HWFLOW) {
                Log.i(TAG, "pkg " + pkg + " is not a history installed application");
            }
        }
    }

    public static final void dumpRecordedAppUsageInfo() {
        mTest.dumpInfo(false);
    }
}
