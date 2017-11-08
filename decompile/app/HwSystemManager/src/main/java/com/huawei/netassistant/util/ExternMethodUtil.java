package com.huawei.netassistant.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import com.huawei.systemmanager.comm.grule.GRuleManager;
import com.huawei.systemmanager.comm.grule.scene.monitor.MonitorScenario;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.util.HwLog;

public class ExternMethodUtil {
    private static final String PACKAGE_NAME_SETTINGS = "com.android.settings";
    private static final String SETTINGS_CLASS_NAME_METERED_WIFI = "com.android.settings.datausage.DataUsageMeteredSettings";
    private static final String SETTINGS_SUBSETTINGS = "com.android.settings.SubSettings";
    private static final String TAG = "ExternMethodUtil";

    public static boolean needCheckByUid(int uid) {
        String packageName = CommonMethodUtil.getPackageNameByUid(uid);
        if (packageName == null) {
            return false;
        }
        Context cxt = GlobalContext.getContext();
        if (cxt != null) {
            return GRuleManager.getInstance().shouldMonitor(cxt, MonitorScenario.SCENARIO_NETMANAGER, packageName);
        }
        return false;
    }

    public static boolean isAboardVersion() {
        return AbroadUtils.isAbroad();
    }

    public static void jumpToMeteredSettingsActivity(Context context) {
        Intent intent = getMeteredSettingsIntent(context);
        if (intent != null) {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                HwLog.e(TAG, "jumpToMeteredSettingsActivity failed:", e);
            }
        }
    }

    public static Intent getMeteredSettingsIntent(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings", SETTINGS_SUBSETTINGS));
        try {
            Resources res = context.createPackageContext("com.android.settings", 2).getResources();
            intent.putExtra(":android:show_fragment", SETTINGS_CLASS_NAME_METERED_WIFI);
            intent.putExtra(":android:show_fragment_title", res.getIdentifier("data_usage_metered_title", "string", "com.android.settings"));
            intent.putExtra(":settings:show_fragment", SETTINGS_CLASS_NAME_METERED_WIFI);
            intent.putExtra(":settings:show_fragment_title", res.getIdentifier("data_usage_metered_title", "string", "com.android.settings"));
            intent.putExtra(":android:no_headers", true);
            intent.setAction("android.intent.action.MAIN");
            return intent;
        } catch (Exception e) {
            HwLog.e(TAG, "createPackageContext failed:", e);
            return null;
        }
    }

    public static void jumptoMobileNetworkSetting(Context context) {
        Intent intent = getMeteredSettingsIntent(context);
        if (intent != null) {
            context.startActivity(intent);
        }
    }
}
