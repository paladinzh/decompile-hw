package com.android.settings;

import android.content.Context;
import android.os.SystemProperties;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustDisplaySettingsHwBaseImpl extends HwCustDisplaySettingsHwBase {
    Context mContext;

    public HwCustDisplaySettingsHwBaseImpl(Context obj) {
        this.mContext = obj;
    }

    public boolean hideSettingsFontStyle() {
        if (this.mContext != null) {
            return "true".equals(Systemex.getString(this.mContext.getContentResolver(), "hw_hide_font_style"));
        }
        return false;
    }

    public boolean isHideNetworkSpeed() {
        return SystemProperties.getBoolean("ro.config.hw_hideNetworkSpeed", false);
    }
}
