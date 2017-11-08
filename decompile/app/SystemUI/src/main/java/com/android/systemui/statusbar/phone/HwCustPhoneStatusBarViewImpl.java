package com.android.systemui.statusbar.phone;

import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.R;

public class HwCustPhoneStatusBarViewImpl extends HwCustPhoneStatusBarView {
    private static final boolean IS_SUPPORT_HD_VOICE = SystemProperties.getBoolean("ro.config.is_support_hd_voice", false);

    public void addHDVoiceView(LayoutInflater layoutInflater, View view) {
        if (IS_SUPPORT_HD_VOICE) {
            ((LinearLayout) view.findViewById(R.id.system_icon_area)).addView(layoutInflater.inflate(R.layout.hd_voice_view, null), 0);
        }
    }
}
