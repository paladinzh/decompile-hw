package com.huawei.systemmanager.power.util;

import android.app.ApplicationErrorReport.CrashInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.FileUtils;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.power.model.BatteryStatisticsHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.io.IOException;

public class DbmsHelper {
    private static final int MAX_DROPBOX_FILE_LEN = 1048576;
    private static String TAG = "DbmsHelper";

    public static ApplicationInfo getApplicationInfoByPkg(Context context, String pkgname) {
        try {
            return context.getPackageManager().getApplicationInfo(pkgname, 0);
        } catch (NameNotFoundException e) {
            HwLog.e(TAG, "NameNotFoundException: " + pkgname);
            e.printStackTrace();
            return null;
        }
    }

    public static void appendDropBoxProcessHeaders(Context context, String pkgName, StringBuilder sb) {
        if (context != null && pkgName != null && sb != null) {
            synchronized (sb) {
                sb.append("Process: ").append(pkgName).append("\n");
                ApplicationInfo ai = getApplicationInfoByPkg(context, pkgName);
                if (ai == null) {
                    return;
                }
                int flags = ai.flags;
                PackageManager pm = context.getPackageManager();
                sb.append("Flags: 0x").append(Integer.toString(flags, 16)).append("\n");
                sb.append("Package: ").append(pkgName);
                try {
                    PackageInfo pi = PackageManagerWrapper.getPackageInfo(pm, pkgName, 0);
                    if (pi != null) {
                        sb.append(" v").append(pi.versionCode);
                        if (pi.versionName != null) {
                            sb.append(SqlMarker.LEFT_PARENTHESES).append(pi.versionName).append(")");
                        }
                    }
                } catch (NameNotFoundException e) {
                    HwLog.e(TAG, "Error getting package info: " + pkgName, e);
                }
                sb.append("\n");
            }
        }
    }

    public static String processClass(Context context, String pkgName) {
        return "app";
    }

    public static void addLogToDropBox(Context context, String eventType, String pkgName, String subject, String report, File logFile, CrashInfo crashInfo) {
        final String dropboxTag = processClass(context, pkgName) + "_" + eventType;
        final DropBoxManager dbox = (DropBoxManager) context.getSystemService("dropbox");
        if (dbox != null && dbox.isTagEnabled(dropboxTag)) {
            final StringBuilder sb = new StringBuilder(1024);
            appendDropBoxProcessHeaders(context, pkgName, sb);
            sb.append("Build: ").append(Build.FINGERPRINT).append("\n");
            sb.append("ReportType: ").append(report);
            sb.append("\n");
            final File file = logFile;
            final CrashInfo crashInfo2 = crashInfo;
            new Thread("Error dump: " + dropboxTag) {
                public void run() {
                    if (file != null) {
                        try {
                            sb.append(FileUtils.readTextFile(file, 1048576, "\n\n[[TRUNCATED]]"));
                        } catch (IOException e) {
                            HwLog.e(DbmsHelper.TAG, "Error reading " + file, e);
                        }
                    }
                    if (!(crashInfo2 == null || crashInfo2.stackTrace == null)) {
                        sb.append(crashInfo2.stackTrace);
                    }
                    HwLog.e(DbmsHelper.TAG, "addText : " + sb.toString());
                    dbox.addText(dropboxTag, sb.toString());
                }
            }.start();
        }
    }

    public static void logHighPowerApp(Context context, String pkgName, String report) {
        addLogToDropBox(context, BatteryStatisticsHelper.DB_POWER, pkgName, null, report, null, null);
    }
}
