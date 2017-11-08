package com.huawei.systemmanager.comm.misc;

public class HsmMath {
    public static int range(int value, int floor, int ceil) {
        if (value < floor) {
            value = floor;
            return floor;
        } else if (value <= ceil) {
            return value;
        } else {
            value = ceil;
            return ceil;
        }
    }

    public static float dist(float x1, float y1, float x2, float y2) {
        float x = x2 - x1;
        float y = y2 - y1;
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }
}
