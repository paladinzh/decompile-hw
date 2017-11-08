package com.huawei.powergenie.debugtest;

public final class LogUtils {
    private LogUtils() {
    }

    public static boolean c(String tag, String msg) {
        return false;
    }

    public static boolean c(String tag, long msg) {
        return c(tag, Long.toString(msg));
    }
}
