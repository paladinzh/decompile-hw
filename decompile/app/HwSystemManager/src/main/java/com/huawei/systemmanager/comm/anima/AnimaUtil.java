package com.huawei.systemmanager.comm.anima;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

public class AnimaUtil {
    public static final Interpolator AD_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    public static final Interpolator LINE_INTERPOLATOR = new LinearInterpolator();
}
