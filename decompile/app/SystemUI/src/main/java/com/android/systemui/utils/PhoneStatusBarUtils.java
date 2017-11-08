package com.android.systemui.utils;

import android.os.SystemProperties;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

public class PhoneStatusBarUtils {
    public static final Interpolator ALPHA_IN = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    public static final Interpolator ALPHA_OUT = new PathInterpolator(0.0f, 0.0f, 0.8f, 1.0f);
    public static boolean mUserToolbox = SystemProperties.getBoolean("ro.config.hw_toolbox", true);
}
