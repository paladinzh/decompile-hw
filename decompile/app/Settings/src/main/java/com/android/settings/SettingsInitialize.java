package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.deviceinfo.UsbReceiver;
import com.android.settingslib.Utils;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import java.util.List;

public class SettingsInitialize extends BroadcastReceiver {
    public void onReceive(Context context, Intent broadcast) {
        UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(UserHandle.myUserId());
        PackageManager pm = context.getPackageManager();
        managedProfileSetup(context, pm, broadcast, userInfo);
        webviewSettingSetup(context, pm, userInfo);
        if (Utils.isEmuiLite()) {
            Log.i("Settings", "initCategories for the first run : " + broadcast.getAction());
            SettingsDrawerActivity.initCategories(context);
        }
    }

    private void managedProfileSetup(Context context, PackageManager pm, Intent broadcast, UserInfo userInfo) {
        if (userInfo != null && (userInfo.isManagedProfile() || userInfo.isClonedProfile())) {
            Log.i("Settings", "Received broadcast: " + broadcast.getAction() + ". Setting up intent forwarding for managed profile.");
            pm.clearCrossProfileIntentFilters(userInfo.id);
            Intent intent = new Intent();
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setPackage(context.getPackageName());
            List<ResolveInfo> resolvedIntents = pm.queryIntentActivities(intent, 705);
            int count = resolvedIntents.size();
            for (int i = 0; i < count; i++) {
                ResolveInfo info = (ResolveInfo) resolvedIntents.get(i);
                if (!(info.filter == null || info.activityInfo == null || info.activityInfo.metaData == null || !info.activityInfo.metaData.getBoolean("com.android.settings.PRIMARY_PROFILE_CONTROLLED"))) {
                    pm.addCrossProfileIntentFilter(info.filter, userInfo.id, userInfo.profileGroupId, 2);
                }
            }
            pm.setComponentEnabledSetting(new ComponentName(context, HWSettings.class), 2, 1);
            pm.setComponentEnabledSetting(new ComponentName(context, UsbReceiver.class), 2, 1);
        }
    }

    private void webviewSettingSetup(Context context, PackageManager pm, UserInfo userInfo) {
        if (userInfo != null) {
            int i;
            ComponentName settingsComponentName = new ComponentName(context, WebViewImplementation.class);
            if (userInfo.isAdmin()) {
                i = 1;
            } else {
                i = 2;
            }
            pm.setComponentEnabledSetting(settingsComponentName, i, 1);
        }
    }
}
