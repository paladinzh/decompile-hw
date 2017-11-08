package com.huawei.gallery.anim;

import android.os.SystemProperties;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.animation.CubicBezierInterpolator;
import com.huawei.watermark.manager.parse.WMElement;

public class AnimationUtils {
    public static final int DEBUG_ANIM_TIME = SystemProperties.getInt("gallery_debug_anim_time", 500);
    public static final int DEBUG_INTERPOLATOR_TYPE = Utils.clamp(SystemProperties.getInt("gallery_debug_anim_type", 0), 0, INTERPOLATOR.length);
    private static final Interpolator[] INTERPOLATOR = new Interpolator[]{new CubicBezierInterpolator(0.44f, 0.1f, 0.07f, WMElement.CAMERASIZEVALUE1B1), new AccelerateInterpolator(), new DecelerateInterpolator(), new AccelerateDecelerateInterpolator()};

    public static Interpolator getInterpolator() {
        return INTERPOLATOR[DEBUG_INTERPOLATOR_TYPE];
    }
}
