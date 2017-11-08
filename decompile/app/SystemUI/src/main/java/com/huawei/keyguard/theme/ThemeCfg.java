package com.huawei.keyguard.theme;

import android.text.TextUtils;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.io.File;

public class ThemeCfg {
    private static final int LEN_SEP = File.separator.length();
    private static final String[] PRESET_PATH_FOR_MAGAZINE = new String[]{"/data/cust/screenlock/magazine", "/system/screenlock/magazine", "/data/screenlock/magazine", "/data/hw_init/system/screenlock/magazine"};
    private static String sDrawableInTheme = concat("/data/skin/", "unlock/", "drawable/");
    private static String sLayoutInTheme = concat("/data/skin/", "unlock/", "layout/");
    private static String sPresetDirForMagazine = null;
    private static final String separator = File.separator;

    private static final String getMagazinePresetPath(String subDir) {
        if (sPresetDirForMagazine == null) {
            for (int i = 0; i < PRESET_PATH_FOR_MAGAZINE.length; i++) {
                if (isDirectory(PRESET_PATH_FOR_MAGAZINE[i])) {
                    sPresetDirForMagazine = PRESET_PATH_FOR_MAGAZINE[i];
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(sPresetDirForMagazine)) {
            HwLog.e("ThemeCfg", "Magazine Path is not preset");
            return BuildConfig.FLAVOR;
        }
        String path = concat(sPresetDirForMagazine, subDir);
        if (!isDirectory(path)) {
            HwLog.d("ThemeCfg", "Invalide preset path:" + path);
        }
        HwLog.w("ThemeCfg", "getMagazinePresetPath " + path);
        return path;
    }

    public static final String getThemeDescription() {
        return concat("/data/skin/", "description.xml");
    }

    public static final String getUnlockDir() {
        return concat("/data/skin/", "unlock/");
    }

    public static final String getDefaultTheme() {
        return concat("/data/skin/", "unlock/", "theme.xml");
    }

    public static final String getMagazineTheme() {
        return getMagazinePresetPath("theme.xml");
    }

    private static boolean isDirectory(String dirPath) {
        File file = new File(dirPath);
        return file.exists() ? file.isDirectory() : false;
    }

    public static final String getWallpaper() {
        String currentname = HwThemeParser.getInstance().getWallpager();
        if (TextUtils.isEmpty(currentname)) {
            return "unlock_wallpaper_0.jpg";
        }
        return currentname;
    }

    public static String concat(String... args) {
        StringBuilder retPath = new StringBuilder();
        for (String section : args) {
            if (!TextUtils.isEmpty(section)) {
                int size = retPath.length();
                if (size > 0 && retPath.lastIndexOf(separator) + LEN_SEP < size) {
                    retPath.append(separator);
                }
                retPath.append(section);
            }
        }
        return retPath.toString();
    }
}
