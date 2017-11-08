package com.huawei.systemmanager.spacecleanner.receiver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.engine.ScanManager;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.AppCleanUpService;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustAPPDataFilter;
import com.huawei.systemmanager.spacecleanner.engine.trash.IAppTrashInfo;
import com.huawei.systemmanager.spacecleanner.uninstall.UninstalledAppService;
import com.huawei.systemmanager.spacecleanner.utils.AppCleanUpAndStorageNotifyUtils;
import com.huawei.systemmanager.spacecleanner.utils.TrashUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;
import java.util.ArrayList;
import java.util.List;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.deepclean.DeepcleanManager;

public class SpaceCleanReceiver extends HsmBroadcastReceiver {
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATHS = "pathes";
    private static final String ATTR_PKG = "pkg";
    private static final String TAG = "SpaceCleanReceiver";

    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            HwLog.e(TAG, "intent or action is null");
        } else {
            sendToBackground(context.getApplicationContext(), intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        super.doInBackground(context, intent);
        String action = intent.getAction();
        HwLog.i(TAG, "receive intent:" + action);
        if (("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) && !isCurrentUserIdApp(intent)) {
            HwLog.i(TAG, "Not current uid app");
            return;
        }
        checkRarelyUsedApp(context, intent);
        String pkgName = getPkgName(intent);
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "pkgName is null!");
            return;
        }
        if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
            HwLog.i(TAG, "package added: " + pkgName);
            doPkgAdd(pkgName);
        } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
            HwLog.i(TAG, "package removed: " + pkgName);
            checkUninstallAppAction(context, pkgName, isUpdateApk(intent));
            doPkgRemoved(pkgName);
        }
    }

    private boolean isUpdateApk(Intent intent) {
        if (intent == null) {
            return false;
        }
        return intent.getBooleanExtra("android.intent.extra.REPLACING", false);
    }

    private void checkRarelyUsedApp(Context context, Intent intent) {
        if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
            String action = intent.getAction();
            Intent serviceIntent = new Intent(context, AppCleanUpService.class);
            Bundle bundle = new Bundle();
            if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                HwLog.d(TAG, "boot completed");
                bundle.putInt(AppCleanUpAndStorageNotifyUtils.SERVICE_INTENT_ARGS, 2);
            } else if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                if (intent.getData() != null) {
                    pkgName = intent.getData().getSchemeSpecificPart();
                    if (!TextUtils.isEmpty(pkgName)) {
                        bundle.putInt(AppCleanUpAndStorageNotifyUtils.SERVICE_INTENT_ARGS, 0);
                        bundle.putString(AppCleanUpAndStorageNotifyUtils.PACKAGE_NAME_ARGS, pkgName);
                    } else {
                        return;
                    }
                }
                return;
            } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                if (intent.getData() != null) {
                    pkgName = intent.getData().getSchemeSpecificPart();
                    if (!TextUtils.isEmpty(pkgName)) {
                        bundle.putInt(AppCleanUpAndStorageNotifyUtils.SERVICE_INTENT_ARGS, 1);
                        bundle.putString(AppCleanUpAndStorageNotifyUtils.PACKAGE_NAME_ARGS, pkgName);
                    } else {
                        return;
                    }
                }
                return;
            } else if (!AppCleanUpAndStorageNotifyUtils.ACTION_LACK_MEMORY_NOTIFY.equals(action)) {
                if (AppCleanUpAndStorageNotifyUtils.ACTION_NOTIFICATION_BUTTON_CLICK_LEFT.equals(action)) {
                    HwLog.i(TAG, "ACTION_NOTIFICATION_BUTTON_CLICK_LEFT received !");
                    HsmStat.statClickLowStorageNotification();
                    bundle.putInt(AppCleanUpAndStorageNotifyUtils.SERVICE_INTENT_ARGS, 4);
                } else if (AppCleanUpAndStorageNotifyUtils.ACTION_NOTIFICATION_BUTTON_CLICK_RIGHT.equals(action)) {
                    HwLog.i(TAG, "ACTION_NOTIFICATION_BUTTON_CLICK_RIGHT received !");
                    HsmStat.statClickLowStorageNotification();
                    bundle.putInt(AppCleanUpAndStorageNotifyUtils.SERVICE_INTENT_ARGS, 5);
                }
            } else {
                return;
            }
            serviceIntent.putExtras(bundle);
            context.startService(serviceIntent);
        }
    }

    private boolean isCurrentUserIdApp(Intent intent) {
        if (intent.hasExtra("android.intent.extra.UID")) {
            int userId = UserHandle.getUserId(intent.getIntExtra("android.intent.extra.UID", 0));
            if (userId == 0) {
                return true;
            }
            HwLog.d(TAG, "Not the same uid app, userId = " + userId);
            return false;
        }
        HwLog.d(TAG, "without uid info");
        return true;
    }

    private void checkUninstallAppAction(Context context, String pkg, boolean isUpdateApk) {
        if (!TMSEngineFeature.isSupportTMS()) {
            HwLog.i(TAG, "not support TMS");
        } else if (!isUpdateApk) {
            DeepcleanManager deepcleanManager = (DeepcleanManager) ManagerCreatorF.getManager(DeepcleanManager.class);
            if (deepcleanManager != null) {
                deepcleanManager.insertUninstallPkg(pkg);
            }
            IAppTrashInfo appTrash = ScanManager.quickScanAppTrash(context, pkg);
            if (appTrash == null) {
                HwLog.i(TAG, "no uninstalled trash, pkg:" + pkg);
                return;
            }
            List<String> list = appTrash.getFiles();
            if (HsmCollections.isEmpty(list)) {
                HwLog.i(TAG, "no uninstalled trash, pkg:" + pkg);
                return;
            }
            ArrayList<String> result = Lists.newArrayListWithCapacity(list.size());
            for (String path : list) {
                result.add(path);
            }
            HwLog.i(TAG, "get uninstalled trash, pkgName:" + pkg + ",appName:" + appTrash.getAppLabel());
            try {
                Intent intentservice = new Intent();
                intentservice.putExtra("pkg", appTrash.getPackageName());
                intentservice.putExtra("name", appTrash.getAppLabel());
                intentservice.putExtra(ATTR_PATHS, (String[]) result.toArray(new String[result.size()]));
                intentservice.setClass(context, UninstalledAppService.class);
                context.startService(intentservice);
            } catch (Exception e) {
                HwLog.i(TAG, "start activity error" + e.toString());
            }
        }
    }

    private String getPkgName(Intent intent) {
        if (intent == null) {
            HwLog.e(TAG, "getPkgName intent is null ");
            return null;
        }
        Uri uri = intent.getData();
        if (uri != null) {
            return uri.getSchemeSpecificPart();
        }
        HwLog.e(TAG, "getPkgName uri is null!");
        return null;
    }

    private void doPkgAdd(String pkgName) {
        PackageInfo pi = TrashUtils.getPackageInfo(pkgName);
        if (pi == null) {
            HwLog.e(TAG, "doPkgAdd pi is null!");
            return;
        }
        String[] pm = pi.requestedPermissions;
        if (pm == null) {
            HwLog.e(TAG, "doPkgAdd pm is null");
            return;
        }
        boolean validPermission = false;
        for (String permission : pm) {
            if ("com.huawei.systemmanager.permission.APPLY_TRIM_POLICY".equals(permission)) {
                validPermission = true;
                break;
            }
        }
        if (!validPermission) {
            HwLog.e(TAG, "doPkgAdd pi is not valid permission");
        } else if (HwCustAPPDataFilter.isHwApp(pi)) {
            ScanManager.getHwCustData(pi);
        } else {
            HwLog.e(TAG, "doPkgAdd pi is not hw app");
        }
    }

    private void doPkgRemoved(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "doPkgRemoved pkgName is null!");
        } else {
            ScanManager.deleteHwCustData(pkgName);
        }
    }
}
