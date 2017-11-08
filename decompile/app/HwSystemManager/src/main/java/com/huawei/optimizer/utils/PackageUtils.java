package com.huawei.optimizer.utils;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Process;
import com.huawei.optimizer.base.ICommand;
import com.huawei.optimizer.base.ICommand2;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.DXLogHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.File;
import java.util.List;

public class PackageUtils {
    private static final boolean DEBUG = false;
    public static final int FLAG_FORWARD_LOCK = 536870912;
    private static final int SDK_VERSION = VERSION.SDK_INT;
    private static final String TAG = "PackageUtils";

    public static Drawable getAppIcon(Context context, String apkFilepath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageArchiveInfo(apkFilepath, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (pkgInfo == null) {
            return null;
        }
        ApplicationInfo appInfo = pkgInfo.applicationInfo;
        if (VERSION.SDK_INT >= 8) {
            appInfo.sourceDir = apkFilepath;
            appInfo.publicSourceDir = apkFilepath;
        }
        return pm.getApplicationIcon(appInfo);
    }

    public static String getPkgLabel(String pkgName) {
        return HsmPackageManager.getInstance().getLabel(pkgName);
    }

    public static Drawable getPkgIcon(String pkgName) {
        return HsmPackageManager.getInstance().getIcon(pkgName);
    }

    public static int getPkgEnabledState(String pkgName) {
        try {
            return GlobalContext.getContext().getPackageManager().getApplicationEnabledSetting(pkgName);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static boolean isSystemApp(int appInfoFlags) {
        return (appInfoFlags & 1) != 0;
    }

    public static boolean isSystemAppNotUpdated(int appInfoFlags) {
        if ((appInfoFlags & 1) == 0 || (appInfoFlags & 128) != 0) {
            return false;
        }
        return true;
    }

    public static Intent getAppDetailsIntent(String pkgName) {
        if (SDK_VERSION >= 9) {
            Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", pkgName, null));
            return intent;
        }
        intent = new Intent("android.intent.action.VIEW");
        intent.setClassName(HsmStatConst.SETTING_PACKAGE_NAME, "com.android.settings.InstalledAppDetails");
        if (SDK_VERSION == 8) {
            intent.putExtra("pkg", pkgName);
            return intent;
        }
        intent.putExtra("com.android.settings.ApplicationPkgName", pkgName);
        return intent;
    }

    public static Intent getRunningServicesIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(HsmStatConst.SETTING_PACKAGE_NAME, "com.android.settings.RunningServices"));
        return intent;
    }

    public static Intent getWirelessSettingsIntent() {
        return new Intent("android.settings.WIRELESS_SETTINGS");
    }

    public static String getMainActivity(Context cxt, String pkgName) {
        try {
            Intent resolveIntent = new Intent("android.intent.action.MAIN");
            resolveIntent.addCategory("android.intent.category.LAUNCHER");
            resolveIntent.setPackage(pkgName);
            ResolveInfo result = PackageManagerWrapper.resolveActivity(cxt.getPackageManager(), resolveIntent, 0);
            if (result == null) {
                resolveIntent.removeCategory("android.intent.category.LAUNCHER");
                resolveIntent.addCategory("android.intent.category.HOME");
                result = PackageManagerWrapper.resolveActivity(cxt.getPackageManager(), resolveIntent, 0);
            }
            if (result != null) {
                return result.activityInfo.name;
            }
        } catch (Exception e) {
            DXLogHelper.w(TAG, "Failed to get main activity of the app: " + pkgName, e);
        }
        return null;
    }

    public static Intent getStartUpIntent(Context ctx, String pkgName) {
        return ctx.getPackageManager().getLaunchIntentForPackage(pkgName);
    }

    public static boolean startupApp(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            if (intent != null) {
                intent.setFlags(270532608);
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            DXLogHelper.w(TAG, "Failed to startup the app: " + pkgName, e);
        }
        return false;
    }

    public static void openInstalledDetail(Context context, String pkgName) {
        Intent intent = getAppDetailsIntent(pkgName);
        if (IntentUtils.isActivityAvailable(context, intent)) {
            intent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
            context.startActivity(intent);
        }
    }

    @TargetApi(14)
    public static void openInstaller(Context cxt, String filepath) {
        Intent intent = new Intent("android.intent.action.INSTALL_PACKAGE", Uri.fromFile(new File(filepath)));
        if (!IntentUtils.isActivityAvailable(cxt, intent)) {
            intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.fromFile(new File(filepath)), "application/vnd.android.package-archive");
        }
        intent.setFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        cxt.startActivity(intent);
    }

    @TargetApi(14)
    public static void openUninstaller(Context context, String pkgName, boolean newTask) {
        Intent uninstallIntent = new Intent("android.intent.action.UNINSTALL_PACKAGE", Uri.parse("package:" + pkgName));
        if (!IntentUtils.isActivityAvailable(context, uninstallIntent)) {
            uninstallIntent.setAction("android.intent.action.DELETE");
        }
        if (newTask) {
            uninstallIntent.setFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
        }
        if (IntentUtils.isActivityAvailable(context, uninstallIntent)) {
            context.startActivity(uninstallIntent);
        } else {
            HwLog.e(TAG, "this activity is not available");
        }
    }

    public static void installAppAsync(Context cxt, String filepath, ICommand preInstallCallback, ICommand2 postInstallCallback) {
        final Context appContext = cxt.getApplicationContext();
        final ICommand iCommand = preInstallCallback;
        final String str = filepath;
        final ICommand2 iCommand2 = postInstallCallback;
        new Thread("Optimizer_installAppAsync") {
            public void run() {
                Process.setThreadPriority(10);
                if (iCommand != null) {
                    iCommand.execute();
                }
                boolean installed = false;
                if (RootUtils.hasRootPermission()) {
                    installed = RootUtils.doInstallApp(str);
                }
                if (!installed) {
                    PackageUtils.openInstaller(appContext, str);
                }
                if (iCommand2 != null) {
                    iCommand2.execute(Boolean.valueOf(installed));
                }
            }
        }.start();
    }

    public static boolean installApp(Context cxt, String filepath) {
        boolean installed = false;
        if (RootUtils.hasRootPermission()) {
            installed = RootUtils.doInstallApp(filepath);
        }
        if (!installed) {
            openInstaller(cxt, filepath);
        }
        return installed;
    }

    public static boolean uninstallApp(Context cxt, String pkgName, boolean newTask) {
        boolean uninstalled = false;
        if (RootUtils.hasRootPermission()) {
            uninstalled = RootUtils.doUninstallApp(pkgName);
        }
        if (!uninstalled) {
            openUninstaller(cxt, pkgName, newTask);
        }
        return uninstalled;
    }

    public static void deletePackage(Context ctx, List<String> pNameList) {
        deletePackage(ctx, pNameList, null);
    }

    public static void deletePackage(Context ctx, List<String> pNameList, IPackageDeleteObserver o) {
        if (pNameList == null || pNameList.isEmpty()) {
            HwLog.i(TAG, "deletepackge list is empty");
            return;
        }
        PackageManager packageMamanger = ctx.getPackageManager();
        for (String pName : pNameList) {
            try {
                packageMamanger.deletePackage(pName, o, 0);
            } catch (Exception e) {
                HwLog.i(TAG, "delete package failed!");
            }
        }
    }
}
