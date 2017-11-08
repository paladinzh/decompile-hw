package com.android.settings;

import android.content.Context;
import com.huawei.android.provider.SettingsEx.Systemex;

public class HwCustFontSettingsImpl extends HwCustFontSettings {
    public HwCustFontSettingsImpl(Context context) {
        super(context);
    }

    public boolean hideSettingsFontStyle() {
        if (this.mContext != null) {
            return "true".equals(Systemex.getString(this.mContext.getContentResolver(), "hw_hide_font_style"));
        }
        return false;
    }
}
