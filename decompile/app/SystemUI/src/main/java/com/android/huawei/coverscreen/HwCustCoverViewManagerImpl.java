package com.android.huawei.coverscreen;

import android.os.SystemProperties;

public class HwCustCoverViewManagerImpl extends HwCustCoverViewManager {
    private static final boolean IS_SHOW_PIXEL_COVER = SystemProperties.getBoolean("ro.config.cover_max_bright", false);
}
