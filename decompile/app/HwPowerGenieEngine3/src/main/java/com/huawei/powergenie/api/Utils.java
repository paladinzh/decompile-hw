package com.huawei.powergenie.api;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Utils {
    private static final SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    private Utils() {
    }

    public static String formatDate(long date) {
        return mSdf.format(new Date(date));
    }

    public static String formatDuration(long duration) {
        int seconds = (int) Math.floor((double) (duration / 1000));
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (seconds > 86400) {
            days = seconds / 86400;
            seconds -= 86400 * days;
        }
        if (seconds > 3600) {
            hours = seconds / 3600;
            seconds -= hours * 3600;
        }
        if (seconds > 60) {
            minutes = seconds / 60;
            seconds -= minutes * 60;
        }
        StringBuffer buffer = new StringBuffer();
        if (days > 0) {
            buffer.append(days).append("d");
        }
        if (hours > 0) {
            buffer.append(hours).append("h");
        }
        if (minutes > 0) {
            buffer.append(minutes).append("m");
        }
        if (seconds > 0) {
            buffer.append(seconds).append("s");
        }
        if (buffer.length() == 0) {
            buffer.append(0).append("s");
        }
        return buffer.toString();
    }
}
