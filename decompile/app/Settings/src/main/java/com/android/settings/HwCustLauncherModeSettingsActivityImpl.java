package com.android.settings;

import android.content.ComponentName;
import android.content.Context;
import android.os.SystemProperties;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustLauncherModeSettingsActivityImpl extends HwCustLauncherModeSettingsActivity {
    private static final String LAUNCHER3_CLASS_NORMALUI = "com.android.launcher3.Launcher";
    private static final String LAUNCHER3_CLASS_SIMPLEUI = "com.android.launcher3.simpleui.SimpleUILauncher";
    private static final String LAUNCHER3_PACKAGE_NAME = "com.android.launcher3";
    private ComponentName mNormalhome;
    private ComponentName mSimpleui;

    public HwCustLauncherModeSettingsActivityImpl() {
        this.mSimpleui = null;
        this.mNormalhome = null;
        this.mSimpleui = new ComponentName(LAUNCHER3_PACKAGE_NAME, LAUNCHER3_CLASS_SIMPLEUI);
        this.mNormalhome = new ComponentName(LAUNCHER3_PACKAGE_NAME, LAUNCHER3_CLASS_NORMALUI);
    }

    public boolean isNormalFontSize(Context context) {
        return Systemex.getInt(context.getContentResolver(), "hw_normal_fontsize_setting", 0) == 1;
    }

    public boolean isLauncher3Mode() {
        return SystemProperties.getBoolean("ro.config.launcher3_simpleui", false);
    }

    public String getLauncher3PackageName() {
        return LAUNCHER3_PACKAGE_NAME;
    }

    public ComponentName getLauncher3NoramlClass() {
        return this.mNormalhome;
    }

    public ComponentName getLauncher3SimpleClass() {
        return this.mSimpleui;
    }
}
