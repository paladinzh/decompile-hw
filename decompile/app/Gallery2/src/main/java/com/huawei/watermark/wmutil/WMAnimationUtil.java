package com.huawei.watermark.wmutil;

import android.content.Context;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.huawei.watermark.controller.WMCubicBezierInterpolator;
import com.huawei.watermark.manager.parse.WMElement;

public class WMAnimationUtil {
    public static Interpolator getInterpolator(Context context, int animId) {
        Interpolator interpolator = null;
        try {
            interpolator = AnimationUtils.loadInterpolator(context, animId);
        } catch (RuntimeException e) {
        }
        if (interpolator != null) {
            return interpolator;
        }
        if (animId == WMResourceUtil.getAnimid(context, "wm_jar_cubic_bezier_interpolator_type_a")) {
            return new WMCubicBezierInterpolator(0.51f, 0.35f, 0.15f, WMElement.CAMERASIZEVALUE1B1);
        }
        return new AccelerateInterpolator();
    }
}
