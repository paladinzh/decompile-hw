package com.android.settings.applications;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import java.util.ArrayList;

public class HwCustManageApplicationsImpl extends HwCustManageApplications {
    public static final String HIDE_APP_KEY = "hw_invisible_apps_in_appmanager";
    public static final boolean IS_THEME_DISABLER = SystemProperties.getBoolean("ro.config.hw_theme_disabler", false);

    public ArrayList<AppEntry> removeThemeApp(ArrayList<AppEntry> mEntries, Context mContext) {
        if (!IS_THEME_DISABLER) {
            return mEntries;
        }
        ArrayList<AppEntry> newEntries = new ArrayList();
        for (int i = 0; i < mEntries.size(); i++) {
            AppEntry entry = (AppEntry) mEntries.get(i);
            if (!entry.info.packageName.equals("com.huawei.android.thememanager")) {
                newEntries.add(entry);
            }
        }
        return newEntries;
    }

    public ArrayList<AppEntry> removeConfigApp(ArrayList<AppEntry> mEntries, Context mContext) {
        String configHideApps = System.getString(mContext.getContentResolver(), HIDE_APP_KEY);
        if (configHideApps == null || "".equals(configHideApps)) {
            return mEntries;
        }
        ArrayList<AppEntry> newEntries = new ArrayList();
        for (int i = 0; i < mEntries.size(); i++) {
            AppEntry entry = (AppEntry) mEntries.get(i);
            if (!configHideApps.contains(entry.info.packageName)) {
                newEntries.add(entry);
            }
        }
        return newEntries;
    }
}
