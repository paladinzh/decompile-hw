package com.huawei.gallery.util;

import android.os.SystemProperties;

public class HwCustGalleryUtils {
    private static int ANIMATION_DURATION = 3000;
    public static final int EXTRA_DURATION = 500;
    private static int SLIDESHOW_DURATION = 3000;
    private static final boolean isSlideShowSettings = SystemProperties.getBoolean("ro.config.slide_show_setting", false);

    public static void setAnimationDuration(int duration) {
        ANIMATION_DURATION = duration;
    }

    public static int getAnimationDuration() {
        return ANIMATION_DURATION + 500;
    }

    public static void setSlideShowDuration(int duration) {
        SLIDESHOW_DURATION = duration;
    }

    public static int getSlideShowDuration() {
        return SLIDESHOW_DURATION;
    }

    public static boolean isSlideshowSettingsSupported() {
        return isSlideShowSettings;
    }
}
