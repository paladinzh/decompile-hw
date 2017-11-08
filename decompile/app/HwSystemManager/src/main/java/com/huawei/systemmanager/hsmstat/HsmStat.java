package com.huawei.systemmanager.hsmstat;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.AdDetect;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.MainScreen;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.NetWorkMgr;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Normal;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.NotificationMgr;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.PermissionMgr;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.PowerSavingMgr;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.SpaceClear;

public class HsmStat {
    private static StatActionImper sStatAction = new StatActionImper();

    public static void statActivityAction(Activity ac, int action) {
        sStatAction.statActivityAction(ac, action);
    }

    public static void statE(String key, String... values) {
        sStatAction.statE(key, values);
    }

    public static void statE(int eventId, String... values) {
        sStatAction.statE(String.valueOf(eventId), values);
    }

    public static void statE(int eventId) {
        sStatAction.statE(String.valueOf(eventId), (String[]) null);
    }

    public static void statMainPageScanButton(int state) {
        sStatAction.statE(MainScreen.ACTION_CLICK_SCAN_BUTTON, "s", String.valueOf(state));
    }

    public static void statNodisturbeSwitch(String action, String packagNameFrom) {
        if (!TextUtils.isEmpty(getSourceFromPackageName(packagNameFrom))) {
            sStatAction.statE("nd", "a", action, "f", getSourceFromPackageName(packagNameFrom));
        }
    }

    public static void statAdUnistallButtonClick(String where) {
        sStatAction.statE(AdDetect.ACTION_CLICK_UNINSTALL, "w", where);
    }

    public static void statPerssmisonDialogAction(String action, int permissionType) {
        sStatAction.statE(PermissionMgr.ACTION_PERMISSION_DIALOG, "a", action, "t", String.valueOf(permissionType));
    }

    public static void statPerssmisonDialogAction(String action, int permissionType, String packageName) {
        sStatAction.statE(PermissionMgr.ACTION_PERMISSION_DIALOG, "a", action, "t", String.valueOf(permissionType), "pm", packageName);
    }

    public static void statPerssmisonSelectAction(String permissionType) {
        statE((int) Events.E_PERMISSION_LISTITEM_CLICK, PermissionMgr.KEY_PERMISSION_SELECT, permissionType);
    }

    public static void statPerssmisonSettingFragmentAction(String action, int permissionType, String packageName) {
        sStatAction.statE(PermissionMgr.ACTION_PERMISSION_LIST, "a", action, "t", String.valueOf(permissionType), "pm", packageName);
    }

    public static void statNetworkAllowDialog(String action, int type) {
        sStatAction.statE(NetWorkMgr.ACTION_NETWORK_PERMISSION_DIALOG, "a", action, "t", String.valueOf(type));
    }

    public static void statClickNotificationNormal(String ac) {
        sStatAction.statE(ac, "");
    }

    public static void statClickLowStorageNotification() {
        sStatAction.statE(SpaceClear.ACTION_CLICK_LOW_STORAGE_NOTIFICATION, new String[0]);
    }

    public static void statClickNotificationFilter(boolean allow) {
        String str;
        StatActionImper statActionImper = sStatAction;
        String str2 = NotificationMgr.ACTION_CLICK_NOTIFICATION_FILTER;
        String[] strArr = new String[2];
        strArr[0] = "a";
        if (allow) {
            str = "a";
        } else {
            str = "f";
        }
        strArr[1] = str;
        statActionImper.statE(str2, strArr);
    }

    public static void statSuperPowerDialogAction(String ac, String whickPackageFrom) {
        if (TextUtils.isEmpty(getSourceFromPackageName(whickPackageFrom))) {
            statE(PowerSavingMgr.ACTION_SUPER_POWER_DIALOG, "a", ac);
            return;
        }
        statE(PowerSavingMgr.ACTION_SUPER_POWER_DIALOG, "a", ac, "f", from);
    }

    public static void checkOnNewIntent(Activity activity, Intent intent) {
        sStatAction.checkOnNewIntent(activity, intent);
    }

    public static boolean isEnable() {
        return sStatAction.isEnable();
    }

    public static boolean setEnable(boolean enbale) {
        return sStatAction.setEnable(enbale);
    }

    public static void init(Application application) {
        sStatAction.initActivityCallBack(application);
    }

    static String getSourceFromPackageName(String from) {
        if (TextUtils.isEmpty(from)) {
            return "";
        }
        String source = Normal.getFromByPackagesName(from);
        if (source == null) {
            return "";
        }
        return source;
    }
}
