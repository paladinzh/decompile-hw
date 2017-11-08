package com.android.gallery3d.ui;

import android.os.SystemClock;

public class AnimationTime {
    private static volatile long sTime;

    public static void update() {
        sTime = SystemClock.uptimeMillis();
    }

    public static long get() {
        return sTime;
    }

    public static long startTime() {
        sTime = SystemClock.uptimeMillis();
        return sTime;
    }
}
