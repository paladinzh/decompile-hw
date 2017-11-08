package com.huawei.systemmanager.spacecleanner.utils;

import com.huawei.systemmanager.comm.misc.GlobalContext;

public class VedioCacheUtils {
    public static final String KEY_RED_POINT = "red_point_key";
    public static final int KEY_RED_POINT_NO = 0;
    public static final int KEY_RED_POINT_YES = 1;
    private static final int KEY_STEP1 = 1;
    public static final String KEY_VIDEO_SIZE = "video_size_key";
    private static final long MAX_VIDEO_TRASH_STEP0 = 524288000;
    private static final long MAX_VIDEO_TRASH_STEP1 = 2147483648L;
    public static final String VEDIO_PREFRENCE = "space_prefence";

    public static boolean isRedPoint() {
        if (1 == getRedPoint()) {
            return true;
        }
        return false;
    }

    public static int getRedPoint() {
        return GlobalContext.getContext().getSharedPreferences("space_prefence", 0).getInt(KEY_RED_POINT, 0);
    }

    public static void saveRedPoint(int key) {
        GlobalContext.getContext().getSharedPreferences("space_prefence", 0).edit().putInt(KEY_RED_POINT, key).commit();
    }

    public static void initRedPoint() {
        saveRedPoint(0);
    }

    public static void saveRedPoint(boolean key) {
        if (key) {
            saveRedPoint(1);
        } else {
            saveRedPoint(0);
        }
    }

    public static long getMaxSize() {
        int key = GlobalContext.getContext().getSharedPreferences("space_prefence", 0).getInt(KEY_VIDEO_SIZE, 0);
        if (key == 0) {
            return 524288000;
        }
        if (1 == key) {
            return 2147483648L;
        }
        if (1 < key) {
            return (((long) (key - 1)) * 2147483648L) + 2147483648L;
        }
        return 0;
    }

    public static void saveSizeKey(long size) {
        GlobalContext.getContext().getSharedPreferences("space_prefence", 0).edit().putInt(KEY_VIDEO_SIZE, getMaxSizeKey(size)).commit();
    }

    private static int getMaxSizeKey(long size) {
        if (size >= 524288000 && size < 2147483648L) {
            return 1;
        }
        if (size >= 2147483648L) {
            return ((int) (size / 2147483648L)) + 1;
        }
        return 0;
    }
}
