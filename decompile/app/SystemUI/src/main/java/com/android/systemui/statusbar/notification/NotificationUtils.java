package com.android.systemui.statusbar.notification;

import android.graphics.Color;
import android.view.View;

public class NotificationUtils {
    private static final int[] sLocationBase = new int[2];
    private static final int[] sLocationOffset = new int[2];

    public static float interpolate(float start, float end, float amount) {
        return ((1.0f - amount) * start) + (end * amount);
    }

    public static int interpolateColors(int startColor, int endColor, float amount) {
        return Color.argb((int) interpolate((float) Color.alpha(startColor), (float) Color.alpha(endColor), amount), (int) interpolate((float) Color.red(startColor), (float) Color.red(endColor), amount), (int) interpolate((float) Color.green(startColor), (float) Color.green(endColor), amount), (int) interpolate((float) Color.blue(startColor), (float) Color.blue(endColor), amount));
    }

    public static float getRelativeYOffset(View offsetView, View baseView) {
        baseView.getLocationOnScreen(sLocationBase);
        offsetView.getLocationOnScreen(sLocationOffset);
        return (float) (sLocationOffset[1] - sLocationBase[1]);
    }
}
