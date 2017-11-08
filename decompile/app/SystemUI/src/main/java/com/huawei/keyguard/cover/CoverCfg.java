package com.huawei.keyguard.cover;

import android.graphics.drawable.Drawable;
import com.huawei.keyguard.util.HwLog;
import java.io.File;

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

    public static boolean isUseThemeOnlineFonts() {
        if (new File("/data/skin/fonts" + File.separator + "DroidSansChinese.ttf").exists()) {
            return true;
        }
        return false;
    }
}
