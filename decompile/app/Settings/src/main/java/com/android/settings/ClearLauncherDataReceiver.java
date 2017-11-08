package com.android.settings;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageDataObserver.Stub;
import android.os.SystemProperties;
import android.util.Log;

public class ClearLauncherDataReceiver extends BroadcastReceiver {

    class ClearUserDataObserver extends Stub {
        ClearUserDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
        }
    }

    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            checkAndClearLauncherData(context);
        }
    }

    private boolean checkAndClearLauncherData(Context context) {
        String historyEmuiVersion = SystemProperties.get("persist.sys.launcher.emuiver", "");
        String curEmuiVersion = SystemProperties.get("ro.build.version.emui", "EmotionUI_0.0");
        boolean allowClearLauncherData = SystemProperties.getBoolean("ro.config.clear_launcher_data", false);
        Log.i("Settings", "persist.sys.launcher.emuiver = " + historyEmuiVersion + ", ro.build.version.emui = " + curEmuiVersion + ", ro.config.clear_launcher_data = " + allowClearLauncherData);
        if (!allowClearLauncherData || !"EmotionUI_4.0".equals(curEmuiVersion) || historyEmuiVersion.equals(curEmuiVersion)) {
            return false;
        }
        Log.i("Settings", "before running clearApplicationUserData()");
        SystemProperties.set("persist.sys.launcher.emuiver", "EmotionUI_4.0");
        boolean res = ((ActivityManager) context.getSystemService("activity")).clearApplicationUserData("com.huawei.android.launcher", new ClearUserDataObserver());
        Log.i("Settings", "run clearApplicationUserData(), res = " + res);
        return res;
    }
}
