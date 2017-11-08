package com.android.huawei.coverscreen;

import android.content.Context;
import android.os.SystemProperties;
import com.android.keyguard.R$drawable;
import fyusion.vislib.BuildConfig;

public class HwCustCoverSwitchPanelImpl extends HwCustCoverSwitchPanel {
    private static final String DAVINCE = "DAVINCE";
    private static final String EDISON = "EDISON";
    private static final float FLING_DISTANCE_SCREEN = 0.225f;
    private static final int FLING_THRESHOLD = 8800;
    private static final float ONE_HALF_SCREEN = 0.45f;
    private static final String RESOURCE_SUFFIX = SystemProperties.get("ro.config.small_cover_size", BuildConfig.FLAVOR);
    private static final int SCREEN_CLOCK_GEM = 1;
    private static final int SCREEN_COUNT_GEM = 3;
    private static final int SCREEN_FIRST = 0;
    private static final int SCREEN_MUSIC_GEM = 2;

    public HwCustCoverSwitchPanelImpl(Context context) {
        super(context);
    }

    public int getNavigationBarResForBright(int defaultId) {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return R$drawable.navigation_bar_bright_for_big;
        }
        return defaultId;
    }

    public int getNavigationBarResForGray(int defaultId) {
        if (RESOURCE_SUFFIX.equals("_1047x1312")) {
            return R$drawable.navigation_bar_gray_for_big;
        }
        return defaultId;
    }

    public boolean isCoverClockViewNeedMask() {
        String str = RESOURCE_SUFFIX;
        if (str.equals("_401x1920") || str.equals("_540x2560") || str.equals("_747x1920") || str.equals("_1440x2560")) {
            return true;
        }
        return false;
    }
}
