package com.android.gallery3d.util;

import android.net.Uri;
import android.os.SystemProperties;
import java.util.Locale;

public final class Constant {
    public static final Locale AR_LOCALE = new Locale("ar", "");
    public static final Locale BN_LOCALE = new Locale("bn", "");
    public static final String CAMERA_PATH = SystemProperties.get("ro.hwcamera.directory", "/DCIM/Camera");
    public static final boolean DBG = (DBG_LEVEL >= 1);
    private static final int DBG_LEVEL = SystemProperties.getInt("gallery-debugable", 0);
    public static final int MAX_ACTION_LAYOUT_NUM = Math.max(4, 5);
    public static final Uri MOVE_OUT_IN_URI = Uri.parse("content://moveoutin");
    public static final Uri MYFAVORITE_URI = Uri.parse("content://myfavorite");
    public static final Locale NE_LOCALE = new Locale("ne", "");
    private static final String[] PLAY_PACKAGE_NAME = new String[]{"com.huawei.himovie", "com.huawei.hwvplayer.youku", "com.huawei.hwvplayer"};
    public static final Uri RELOAD_DISCOVER_LOCATION = Uri.parse("content://reload/discover/location");
    public static final Uri RELOAD_URI_ALBUM = Uri.parse("content://reload/album");
    public static final Uri RELOAD_URI_ALBUMSET = Uri.parse("content://reload/albumset");
    public static final Uri RELOAD_URI_KIDS_ALBUM = Uri.parse("content://reload/kids_album");
    public static final Uri SETTIGNS_URI = Uri.parse("content://settings/gallery");
    public static final Locale UR_LOCALE = new Locale("ur", "");
    public static final boolean VDBG;

    static {
        boolean z;
        if (DBG_LEVEL >= 2) {
            z = true;
        } else {
            z = false;
        }
        VDBG = z;
    }

    public static final String[] getPlayPackageName() {
        return (String[]) PLAY_PACKAGE_NAME.clone();
    }
}
