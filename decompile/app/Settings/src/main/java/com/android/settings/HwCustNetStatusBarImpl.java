package com.android.settings;

import android.os.SystemProperties;
import android.view.View;
import android.widget.ImageView;

public class HwCustNetStatusBarImpl extends HwCustNetStatusBar {
    public void hideSimIconLayout(View view) {
        if ("true".equals(SystemProperties.get("ro.config.hide_simIcon_layout", "false"))) {
            view.setVisibility(8);
        }
    }

    public void changeIconImage(ImageView imageView) {
        if (isFlagSpringPimExtEnabled()) {
            imageView.setImageResource(2130838690);
        }
    }

    private static boolean isFlagSpringPimExtEnabled() {
        return SystemProperties.getBoolean("ro.config.sprint_pim_ext", false);
    }
}
