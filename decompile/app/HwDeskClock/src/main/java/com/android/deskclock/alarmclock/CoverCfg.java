package com.android.deskclock.alarmclock;

import android.graphics.drawable.Drawable;
import com.android.util.HwLog;

public class CoverCfg {
    private static final String[] PRESET_PATH_FOR_COVERSCREEN = new String[]{"/data/skin/wallpaper/", "/data/cust/screenlock/coverscreen/", "/system/screenlock/coverscreen/", "/data/screenlock/coverscreen/", "/data/hw_init/system/screenlock/coverscreen/"};

    public static Drawable getCoverWallpaper() {
        Drawable coverDrawable = null;
        for (String concat : PRESET_PATH_FOR_COVERSCREEN) {
            String filePath = concat.concat("cover_wallpaper_0.jpg");
            coverDrawable = Drawable.createFromPath(filePath);
            if (coverDrawable != null) {
                HwLog.w("ThemeCfg", "getCoverWallpaper success from " + filePath);
                break;
            }
            HwLog.w("ThemeCfg", "getCoverWallpaper from " + filePath + " is null");
        }
        return coverDrawable;
    }
}
