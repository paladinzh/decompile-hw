package com.android.huawei.coverscreen;

import android.content.Context;

public class HwCustCoverSwitchPanel {
    private static final float DEFAULT_PANEL_GET = 0.0f;

    public HwCustCoverSwitchPanel(Context context) {
    }

    public int getNavigationBarResForBright(int defaultId) {
        return defaultId;
    }

    public int getNavigationBarResForGray(int defaultId) {
        return defaultId;
    }

    public boolean isCoverClockViewNeedMask() {
        return false;
    }
}
